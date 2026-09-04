package tui

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/google/uuid"
	"github.com/mralmostcool/tarbook-project/simulator/internal/compression"
	"github.com/mralmostcool/tarbook-project/simulator/internal/mirror"
	"github.com/mralmostcool/tarbook-project/simulator/internal/outbox"
	"github.com/mralmostcool/tarbook-project/simulator/internal/signer"
)

type Role string

const (
	RoleCandidate Role = "CANDIDATE"
	RoleOfficer   Role = "OFFICER"
)

var (
	titleStyle = lipgloss.NewStyle().
			Bold(true).
			Foreground(lipgloss.Color("#FAFAFA")).
			Background(lipgloss.Color("#1B365D")).
			Padding(0, 1).
			MarginBottom(1)

	boxStyle = lipgloss.NewStyle().
			Border(lipgloss.RoundedBorder()).
			BorderForeground(lipgloss.Color("#4A777A")).
			Padding(0, 1).
			MarginRight(1)

	highlightStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#00E676")).
			Bold(true)

	subtleStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#9E9E9E"))

	statusStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#FFD54F")).
			Italic(true)
)

// Model represents the Bubbletea state for the simulator cockpit.
type Model struct {
	Role          Role
	ClientID      string
	DBPath        string
	PendingOutbox int
	AppliedOutbox int
	LatestSyncSeq int64
	OfficerKeyID  string
	OfficerStatus string
	StatusMessage string
	LastBenchmark string
	outboxStore   outbox.Store
	mirrorStore   mirror.Store
	signerSvc     signer.Service
}

// NewModel creates an interactive TUI model.
func NewModel(dbPath string, clientID string) (*Model, error) {
	if clientID == "" {
		clientID = "vessel-sim-edge-01"
	}

	outboxStore, err := outbox.NewSQLiteStore(dbPath)
	if err != nil {
		return nil, fmt.Errorf("failed to open outbox store: %w", err)
	}

	mirrorStore, err := mirror.NewSQLiteMirrorStore(dbPath)
	if err != nil {
		outboxStore.Close()
		return nil, fmt.Errorf("failed to open mirror store: %w", err)
	}

	signerSvc := signer.NewMemorySignerService()
	key, _ := signerSvc.GenerateKey(context.Background(), uuid.New())

	m := &Model{
		Role:          RoleCandidate,
		ClientID:      clientID,
		DBPath:        dbPath,
		OfficerKeyID:  key.KeyID,
		OfficerStatus: string(key.Status),
		StatusMessage: "Simulator ready. Select action.",
		outboxStore:   outboxStore,
		mirrorStore:   mirrorStore,
		signerSvc:     signerSvc,
	}
	m.RefreshCounts()
	return m, nil
}

func (m *Model) RefreshCounts() {
	ctx := context.Background()
	pending, err := m.outboxStore.GetPending(ctx, 1000)
	if err == nil {
		m.PendingOutbox = len(pending)
	}

	seq, err := m.mirrorStore.GetLastSyncSequence(ctx)
	if err == nil {
		m.LatestSyncSeq = seq
	}
}

func (m *Model) Close() error {
	var errs []string
	if m.outboxStore != nil {
		if err := m.outboxStore.Close(); err != nil {
			errs = append(errs, err.Error())
		}
	}
	if m.mirrorStore != nil {
		if err := m.mirrorStore.Close(); err != nil {
			errs = append(errs, err.Error())
		}
	}
	if len(errs) > 0 {
		return fmt.Errorf("%s", strings.Join(errs, "; "))
	}
	return nil
}

func (m *Model) Init() tea.Cmd {
	return nil
}

func (m *Model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.KeyMsg:
		switch msg.String() {
		case "ctrl+c", "q":
			return m, tea.Quit

		case "c":
			m.Role = RoleCandidate
			m.StatusMessage = "Switched to CANDIDATE mode."

		case "o":
			m.Role = RoleOfficer
			m.StatusMessage = "Switched to OFFICER mode."

		case "e":
			ctx := context.Background()
			taskOp := outbox.OperationEnvelope{
				OperationID: uuid.New(),
				EntityType:  "task_entries",
				Action:      "INSERT",
				Payload:     json.RawMessage(`{"title":"Bridge Watchkeeping Navigation","vessel_imo":"9876543"}`),
				CreatedAt:   time.Now().UTC(),
			}
			if err := m.outboxStore.Enqueue(ctx, taskOp); err != nil {
				m.StatusMessage = fmt.Sprintf("Error enqueuing task: %v", err)
			} else {
				m.StatusMessage = fmt.Sprintf("Candidate logged task %s", taskOp.OperationID.String()[:8])
				m.RefreshCounts()
			}

		case "s":
			ctx := context.Background()
			signoffMap := map[string]interface{}{
				"task_entry_id": uuid.New().String(),
				"signer_role":   "SUPERVISING_OFFICER",
				"verdict":       "COMPETENT",
				"signed_at_utc": time.Now().UTC().Format(time.RFC3339Nano),
			}
			raw, _ := json.Marshal(signoffMap)
			sig, err := m.signerSvc.SignCanonical(ctx, m.OfficerKeyID, raw)
			if err != nil {
				m.StatusMessage = fmt.Sprintf("Officer signing error: %v", err)
			} else {
				signoffPayload, _ := json.Marshal(map[string]interface{}{
					"signoff":        signoffMap,
					"key_id":         m.OfficerKeyID,
					"signature_asn1": sig,
				})
				_ = m.outboxStore.Enqueue(ctx, outbox.OperationEnvelope{
					OperationID: uuid.New(),
					EntityType:  "task_signoffs",
					Action:      "INSERT",
					Payload:     signoffPayload,
					CreatedAt:   time.Now().UTC(),
				})
				m.StatusMessage = fmt.Sprintf("Officer signed competency assessment (ECDSA P-256)")
				m.RefreshCounts()
			}

		case "b":
			payloads := compression.GenerateRealisticPayloads()
			results, err := compression.RunBenchmark(payloads, 5)
			if err != nil {
				m.StatusMessage = fmt.Sprintf("Benchmark error: %v", err)
			} else {
				var b strings.Builder
				for _, r := range results {
					if r.Algorithm == "zstd" {
						b.WriteString(fmt.Sprintf("%s: %.1f%% savings; ", r.PayloadName[:7], r.SavingsPercent))
					}
				}
				m.LastBenchmark = b.String()
				m.StatusMessage = "Completed compression benchmark."
			}
		}
	}

	return m, nil
}

func (m *Model) View() string {
	var b strings.Builder

	b.WriteString(titleStyle.Render("⚓ PROJECT TARBOOK - MARITIME EDGE SIMULATOR (ADR 0008)"))
	b.WriteString("\n\n")

	// Left Box: Edge Node & Role
	roleDisplay := highlightStyle.Render(string(m.Role))
	nodeInfo := fmt.Sprintf("Node ID: %s\nRole:    %s\nDB Path: %s",
		m.ClientID, roleDisplay, m.DBPath)
	leftBox := boxStyle.Render(nodeInfo)

	// Middle Box: Outbox & Mirror Status
	syncInfo := fmt.Sprintf("Outbox Pending: %s\nAuthoritative Sync Seq: %s\nLatest Benchmark: %s",
		highlightStyle.Render(fmt.Sprintf("%d ops", m.PendingOutbox)),
		highlightStyle.Render(fmt.Sprintf("%d", m.LatestSyncSeq)),
		subtleStyle.Render(m.LastBenchmark))
	midBox := boxStyle.Render(syncInfo)

	// Right Box: Officer PKI Status
	pkiInfo := fmt.Sprintf("Key ID:  %s\nStatus:  %s\nType:    %s",
		m.OfficerKeyID[:15]+"...",
		highlightStyle.Render(m.OfficerStatus),
		subtleStyle.Render("ECDSA_P256"))
	rightBox := boxStyle.Render(pkiInfo)

	b.WriteString(lipgloss.JoinHorizontal(lipgloss.Top, leftBox, midBox, rightBox))
	b.WriteString("\n\n")

	// Status Line
	b.WriteString(statusStyle.Render("Status: " + m.StatusMessage))
	b.WriteString("\n\n")

	// Keybind Controls
	b.WriteString(subtleStyle.Render("[c] Switch Candidate  [o] Switch Officer  [e] Enqueue Task  [s] Sign Assessment  [b] Benchmark  [q] Quit"))
	b.WriteString("\n")

	return b.String()
}
