package evidence

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"net/http"
	"time"
)

type httpObjectUploader struct {
	client *http.Client
}

// NewHTTPObjectUploader provides an ObjectUploader implementation for S3/MinIO presigned PUT requests.
func NewHTTPObjectUploader(client *http.Client) ObjectUploader {
	if client == nil {
		client = http.DefaultClient
	}
	return &httpObjectUploader{client: client}
}

func (u *httpObjectUploader) UploadObject(ctx context.Context, uploadURL string, headers map[string]string, content io.Reader, size int64) error {
	req, err := http.NewRequestWithContext(ctx, http.MethodPut, uploadURL, content)
	if err != nil {
		return fmt.Errorf("failed to create HTTP upload request: %w", err)
	}
	req.ContentLength = size
	for k, v := range headers {
		req.Header.Set(k, v)
	}

	resp, err := u.client.Do(req)
	if err != nil {
		return fmt.Errorf("object upload HTTP transport error: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return fmt.Errorf("object storage returned non-2xx status code: %d", resp.StatusCode)
	}

	return nil
}

type evidenceUploadService struct {
	objUploader ObjectUploader
	refresher   AuthorizationRefresher
	reporter    CompletionReporter
}

// NewEvidenceUploader initializes the evidence upload coordinator seam.
func NewEvidenceUploader(objUploader ObjectUploader, refresher AuthorizationRefresher, reporter CompletionReporter) EvidenceUploader {
	return &evidenceUploadService{
		objUploader: objUploader,
		refresher:   refresher,
		reporter:    reporter,
	}
}

func (s *evidenceUploadService) Upload(ctx context.Context, evidence LocalEvidence, auth *UploadAuthorization, content io.Reader) (*UploadReport, error) {
	// 1. Read and calculate SHA-256 checksum over content without modifying metadata
	buf, err := io.ReadAll(content)
	if err != nil {
		return &UploadReport{
			ArtifactID: evidence.ID,
			Status:     StatusUploadFailed,
			Error:      err,
		}, fmt.Errorf("failed to read binary evidence content: %w", err)
	}

	h := sha256.Sum256(buf)
	calculatedChecksum := hex.EncodeToString(h[:])

	// Invariant 5: Checksum mismatch must not delete metadata or alter the expected hash
	if calculatedChecksum != evidence.SHA256Checksum {
		return &UploadReport{
			ArtifactID: evidence.ID,
			Status:     StatusUploadFailed,
			Error:      ErrChecksumMismatch,
		}, fmt.Errorf("%w: expected %s, got %s", ErrChecksumMismatch, evidence.SHA256Checksum, calculatedChecksum)
	}

	// 2. Obtain or refresh upload authorization if missing or expired
	currentAuth := auth
	refreshedURL := false
	now := time.Now().UTC()

	if currentAuth == nil || currentAuth.IsExpired(now) {
		if s.refresher == nil {
			return &UploadReport{
				ArtifactID: evidence.ID,
				Status:     StatusUploadFailed,
				Error:      ErrMissingAuthorization,
			}, ErrMissingAuthorization
		}

		freshAuth, err := s.refresher.RefreshUploadAuthorization(ctx, evidence.ID)
		if err != nil {
			return &UploadReport{
				ArtifactID: evidence.ID,
				Status:     StatusUploadFailed,
				Error:      err,
			}, fmt.Errorf("failed to refresh upload authorization: %w", err)
		}
		currentAuth = freshAuth
		refreshedURL = true
	}

	// 3. Binary upload directly to S3/MinIO presigned URL
	if err := s.objUploader.UploadObject(ctx, currentAuth.UploadURL, currentAuth.Headers, bytes.NewReader(buf), int64(len(buf))); err != nil {
		return &UploadReport{
			ArtifactID:   evidence.ID,
			Status:       StatusUploadFailed,
			RefreshedURL: refreshedURL,
			Error:        err,
		}, fmt.Errorf("binary object upload failed: %w", err)
	}

	// 4. Server-side verification and completion reporting
	if s.reporter == nil {
		return &UploadReport{
			ArtifactID:   evidence.ID,
			Status:       StatusUploaded,
			RefreshedURL: refreshedURL,
		}, nil
	}

	verification, err := s.reporter.ReportUploadComplete(ctx, evidence.ID, evidence.SHA256Checksum)
	if err != nil {
		// Binary is in object storage, but server verification is pending/failed
		return &UploadReport{
			ArtifactID:   evidence.ID,
			Status:       StatusUploaded,
			RefreshedURL: refreshedURL,
			Error:        err,
		}, fmt.Errorf("completion reporting failed: %w", err)
	}

	verified := verification.Status == StatusVerified
	return &UploadReport{
		ArtifactID:   evidence.ID,
		Status:       verification.Status,
		Verified:     verified,
		RefreshedURL: refreshedURL,
	}, nil
}
