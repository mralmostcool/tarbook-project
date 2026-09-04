package main

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"path/filepath"
	"text/tabwriter"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/mralmostcool/tarbook-project/simulator/internal/compression"
	"github.com/mralmostcool/tarbook-project/simulator/internal/scenario"
	"github.com/mralmostcool/tarbook-project/simulator/internal/tui"
)

func main() {
	if len(os.Args) < 2 {
		runCockpit("tarbook_sim.db", "vessel-sim-node-01")
		return
	}

	subcommand := os.Args[1]
	switch subcommand {
	case "cockpit":
		fs := flag.NewFlagSet("cockpit", flag.ExitOnError)
		dbPath := fs.String("db", "tarbook_sim.db", "Path to SQLite database file")
		clientID := fs.String("client-id", "vessel-sim-node-01", "Client device node ID")
		_ = fs.Parse(os.Args[2:])
		runCockpit(*dbPath, *clientID)

	case "benchmark":
		fs := flag.NewFlagSet("benchmark", flag.ExitOnError)
		iterations := fs.Int("iterations", 20, "Number of compression iterations per payload")
		asJSON := fs.Bool("json", false, "Output results as JSON")
		_ = fs.Parse(os.Args[2:])
		runBenchmark(*iterations, *asJSON)

	case "run":
		fs := flag.NewFlagSet("run", flag.ExitOnError)
		scen := fs.String("scenario", "dual", "Simulation scenario (candidate, officer, dual)")
		backend := fs.String("backend", "", "Shore Sync backend base URL (e.g. http://localhost:8080)")
		dbPath := fs.String("db", "", "Path to SQLite database file (defaults to temp db)")
		clientID := fs.String("client-id", "vessel-sim-node-01", "Client device node ID")
		asJSON := fs.Bool("json", false, "Output results as JSON")
		_ = fs.Parse(os.Args[2:])
		runHeadless(*scen, *backend, *dbPath, *clientID, *asJSON)

	case "-h", "--help", "help":
		printUsage()

	default:
		fmt.Fprintf(os.Stderr, "Unknown command: %s\n\n", subcommand)
		printUsage()
		os.Exit(1)
	}
}

func printUsage() {
	fmt.Println("⚓ Project Tarbook Maritime Edge Device Simulator (ADR 0008)")
	fmt.Println()
	fmt.Println("Usage:")
	fmt.Println("  tarbook-sim [cockpit]     Launch interactive Charm Bubbletea TUI cockpit")
	fmt.Println("  tarbook-sim benchmark     Run empirical gzip vs zstd compression comparison")
	fmt.Println("  tarbook-sim run           Execute scriptable headless simulation scenario")
	fmt.Println()
	fmt.Println("Flags for 'run':")
	fmt.Println("  --scenario=<name>         Scenario to execute (candidate, officer, dual) [default: dual]")
	fmt.Println("  --backend=<url>           Shore Sync backend endpoint (optional)")
	fmt.Println("  --db=<path>               SQLite storage path (default: temporary in-memory db)")
	fmt.Println("  --client-id=<id>          Client device node identifier")
	fmt.Println("  --json                    Print structured JSON report to stdout")
	fmt.Println()
	fmt.Println("Flags for 'benchmark':")
	fmt.Println("  --iterations=<n>          Number of iterations per payload [default: 20]")
	fmt.Println("  --json                    Print structured JSON output")
}

func runCockpit(dbPath, clientID string) {
	model, err := tui.NewModel(dbPath, clientID)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to initialize cockpit: %v\n", err)
		os.Exit(1)
	}
	defer model.Close()

	p := tea.NewProgram(model)
	if _, err := p.Run(); err != nil {
		fmt.Fprintf(os.Stderr, "Cockpit runtime error: %v\n", err)
		os.Exit(1)
	}
}

func runBenchmark(iterations int, asJSON bool) {
	payloads := compression.GenerateRealisticPayloads()
	results, err := compression.RunBenchmark(payloads, iterations)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Benchmark error: %v\n", err)
		os.Exit(1)
	}

	if asJSON {
		enc := json.NewEncoder(os.Stdout)
		enc.SetIndent("", "  ")
		_ = enc.Encode(results)
		return
	}

	fmt.Println("==========================================================================================")
	fmt.Println("⚓ PROJECT TARBOOK - EMPIRICAL COMPRESSION BENCHMARK (ADR 0003 / ADR 0007)")
	fmt.Println("==========================================================================================")
	w := tabwriter.NewWriter(os.Stdout, 0, 0, 3, ' ', 0)
	fmt.Fprintln(w, "PAYLOAD\tALGO\tORIGINAL\tCOMPRESSED\tSAVINGS\tCOMPRESS\tDECOMPRESS")
	fmt.Fprintln(w, "-------\t----\t--------\t----------\t-------\t--------\t----------")

	for _, r := range results {
		fmt.Fprintf(w, "%s\t%s\t%d B\t%d B\t%.1f%%\t%s\t%s\n",
			r.PayloadName,
			r.Algorithm,
			r.OriginalBytes,
			r.CompressedBytes,
			r.SavingsPercent,
			r.CompressDuration,
			r.DecompressDuration,
		)
	}
	_ = w.Flush()
	fmt.Println("==========================================================================================")
}

func runHeadless(scenarioName, backendURL, dbPath, clientID string, asJSON bool) {
	if dbPath == "" {
		tempDir, err := os.MkdirTemp("", "tarbook_headless_*")
		if err != nil {
			fmt.Fprintf(os.Stderr, "Failed to create temp directory: %v\n", err)
			os.Exit(1)
		}
		defer os.RemoveAll(tempDir)
		dbPath = filepath.Join(tempDir, "headless.db")
	}

	cfg := scenario.Config{
		Scenario:   scenarioName,
		BackendURL: backendURL,
		DBPath:     dbPath,
		ClientID:   clientID,
	}

	report, err := scenario.Run(context.Background(), cfg)
	if asJSON {
		enc := json.NewEncoder(os.Stdout)
		enc.SetIndent("", "  ")
		_ = enc.Encode(report)
	} else {
		fmt.Printf("Scenario:          %s\n", report.Scenario)
		fmt.Printf("Client Node:       %s\n", report.ClientID)
		fmt.Printf("Enqueued Ops:      %d\n", report.EnqueuedOps)
		fmt.Printf("Signed Ops:        %d\n", report.SignedOps)
		fmt.Printf("Pushed Ops:        %d\n", report.PushedOps)
		fmt.Printf("Pulled Deltas:     %d\n", report.PulledDeltas)
		fmt.Printf("Success:           %t\n", report.Success)
		if report.Error != "" {
			fmt.Printf("Error:             %s\n", report.Error)
		}
	}

	if err != nil || !report.Success {
		os.Exit(1)
	}
}
