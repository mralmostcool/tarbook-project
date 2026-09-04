package tui

import (
	"path/filepath"
	"strings"
	"testing"

	tea "github.com/charmbracelet/bubbletea"
)

func TestCockpitModel_KeyTransitions(t *testing.T) {
	tempDir := t.TempDir()
	dbPath := filepath.Join(tempDir, "cockpit_test.db")

	model, err := NewModel(dbPath, "test-vessel-01")
	if err != nil {
		t.Fatalf("failed to initialize model: %v", err)
	}
	defer model.Close()

	if model.Role != RoleCandidate {
		t.Fatalf("expected initial role CANDIDATE, got %s", model.Role)
	}

	// 1. Test role switch to Officer ('o')
	m, _ := model.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{'o'}})
	model = m.(*Model)
	if model.Role != RoleOfficer {
		t.Fatalf("expected role OFFICER, got %s", model.Role)
	}

	// 2. Test candidate task enqueue ('e')
	m, _ = model.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{'e'}})
	model = m.(*Model)
	if model.PendingOutbox != 1 {
		t.Fatalf("expected 1 pending outbox op, got %d", model.PendingOutbox)
	}

	// 3. Test officer assessment signing ('s')
	m, _ = model.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{'s'}})
	model = m.(*Model)
	if model.PendingOutbox != 2 {
		t.Fatalf("expected 2 pending outbox ops after signoff, got %d", model.PendingOutbox)
	}

	// 4. Test benchmark execution ('b')
	m, _ = model.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{'b'}})
	model = m.(*Model)
	if model.LastBenchmark == "" {
		t.Fatalf("expected benchmark results populated")
	}

	// 5. Test view rendering
	view := model.View()
	if !strings.Contains(view, "TARBOOK") {
		t.Fatalf("view missing TARBOOK header: %s", view)
	}
	if !strings.Contains(view, "OFFICER") {
		t.Fatalf("view missing OFFICER role: %s", view)
	}

	// 6. Test quit ('q')
	_, cmd := model.Update(tea.KeyMsg{Type: tea.KeyRunes, Runes: []rune{'q'}})
	if cmd == nil {
		t.Fatalf("expected quit command, got nil")
	}
}
