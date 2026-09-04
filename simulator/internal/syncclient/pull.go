package syncclient

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"net/url"
	"strconv"
	"time"

	"github.com/google/uuid"
)

var (
	ErrSequenceRegression = errors.New("server returned non-monotonic or regressing sync sequence")
)

// DeltaItem represents a single authoritative mutation pulled from the server.
type DeltaItem struct {
	SyncSequence int64           `json:"sync_sequence"`
	OperationID  uuid.UUID       `json:"operation_id"`
	EntityType   string          `json:"entity_type"`
	EntityID     uuid.UUID       `json:"entity_id"`
	Action       string          `json:"action"`
	Payload      json.RawMessage `json:"payload"`
	CommittedAt  time.Time       `json:"committed_at"`
}

// SyncPullResponse represents the cursor-paginated response wire format from GET /api/v1/sync/pull.
type SyncPullResponse struct {
	Items            []DeltaItem `json:"items"`
	HasMore          bool        `json:"has_more"`
	NextSyncSequence int64       `json:"next_sync_sequence"`
}

// PullReport summarizes the local outcome of a delta pull operation.
type PullReport struct {
	ItemsApplied     int
	HasMore          bool
	NextSyncSequence int64
}

// MirrorStore defines the persistence interface for storing authoritative entity replicas locally.
type MirrorStore interface {
	GetLastSyncSequence(ctx context.Context) (int64, error)
	ApplyDeltas(ctx context.Context, items []DeltaItem, nextSyncSequence int64) error
}

// PullClient defines the client seam for fetching authoritative deltas from shore.
type PullClient interface {
	Pull(ctx context.Context, limit int) (*PullReport, error)
}

type httpPullClient struct {
	clientID   string
	mirror     MirrorStore
	baseURL    string
	httpClient *http.Client
}

// NewPullClient creates an HTTP pull client adapter.
func NewPullClient(clientID string, mirror MirrorStore, baseURL string, httpClient *http.Client) PullClient {
	if httpClient == nil {
		httpClient = http.DefaultClient
	}
	return &httpPullClient{
		clientID:   clientID,
		mirror:     mirror,
		baseURL:    baseURL,
		httpClient: httpClient,
	}
}

func (c *httpPullClient) Pull(ctx context.Context, limit int) (*PullReport, error) {
	sinceSeq, err := c.mirror.GetLastSyncSequence(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to retrieve last sync sequence from mirror: %w", err)
	}

	endpoint, err := url.JoinPath(c.baseURL, "/api/v1/sync/pull")
	if err != nil {
		return nil, fmt.Errorf("invalid endpoint URL: %w", err)
	}

	reqURL, err := url.Parse(endpoint)
	if err != nil {
		return nil, fmt.Errorf("failed to parse endpoint URL: %w", err)
	}

	query := reqURL.Query()
	query.Set("since", strconv.FormatInt(sinceSeq, 10))
	if limit > 0 {
		query.Set("limit", strconv.Itoa(limit))
	}
	reqURL.RawQuery = query.Encode()

	httpReq, err := http.NewRequestWithContext(ctx, http.MethodGet, reqURL.String(), nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create HTTP pull request: %w", err)
	}

	httpReq.Header.Set("Accept", "application/json")
	httpReq.Header.Set("X-Client-Id", c.clientID)

	resp, err := c.httpClient.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("sync pull HTTP transport error: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("%w: HTTP %d", ErrUntrustworthyReceipt, resp.StatusCode)
	}

	var pullResp SyncPullResponse
	if err := json.NewDecoder(resp.Body).Decode(&pullResp); err != nil {
		return nil, fmt.Errorf("%w: failed to decode pull response: %v", ErrUntrustworthyReceipt, err)
	}

	// Invariant: Sequence monotonicity. Next sequence cannot regress behind current local sequence.
	if pullResp.NextSyncSequence < sinceSeq {
		return nil, fmt.Errorf("%w: received next sequence %d lower than current %d",
			ErrSequenceRegression, pullResp.NextSyncSequence, sinceSeq)
	}

	// Verify all items are strictly greater than sinceSeq
	for _, item := range pullResp.Items {
		if item.SyncSequence <= sinceSeq {
			return nil, fmt.Errorf("%w: item sequence %d not strictly greater than since cursor %d",
				ErrSequenceRegression, item.SyncSequence, sinceSeq)
		}
	}

	if err := c.mirror.ApplyDeltas(ctx, pullResp.Items, pullResp.NextSyncSequence); err != nil {
		return nil, fmt.Errorf("failed to apply incoming deltas to local mirror: %w", err)
	}

	return &PullReport{
		ItemsApplied:     len(pullResp.Items),
		HasMore:          pullResp.HasMore,
		NextSyncSequence: pullResp.NextSyncSequence,
	}, nil
}
