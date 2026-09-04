package compression

import (
	"encoding/json"
	"fmt"
	"sort"
	"time"

	"github.com/google/uuid"
)

// BenchmarkResult encapsulates performance and ratio metrics for an algorithm on a specific payload.
type BenchmarkResult struct {
	PayloadName        string        `json:"payload_name"`
	Algorithm          string        `json:"algorithm"`
	OriginalBytes      int           `json:"original_bytes"`
	CompressedBytes    int           `json:"compressed_bytes"`
	SavingsPercent     float64       `json:"savings_percent"`
	CompressionRatio   float64       `json:"compression_ratio"`
	CompressDuration   time.Duration `json:"compress_duration"`
	DecompressDuration time.Duration `json:"decompress_duration"`
}

// RunBenchmark executes empirical compression comparison across provided payloads.
func RunBenchmark(payloads map[string][]byte, iterations int) ([]BenchmarkResult, error) {
	if iterations <= 0 {
		iterations = 10
	}

	var results []BenchmarkResult

	// Sort payload keys for deterministic output
	keys := make([]string, 0, len(payloads))
	for k := range payloads {
		keys = append(keys, k)
	}
	sort.Strings(keys)

	for _, name := range keys {
		data := payloads[name]
		origSize := len(data)

		// 1. Benchmark Gzip
		var gzipCompressed []byte
		var gzipCompressDuration, gzipDecompressDuration time.Duration

		startComp := time.Now()
		for i := 0; i < iterations; i++ {
			var err error
			gzipCompressed, err = CompressGzip(data)
			if err != nil {
				return nil, fmt.Errorf("gzip benchmark error on %s: %w", name, err)
			}
		}
		gzipCompressDuration = time.Since(startComp) / time.Duration(iterations)

		startDecomp := time.Now()
		for i := 0; i < iterations; i++ {
			if _, err := DecompressGzip(gzipCompressed); err != nil {
				return nil, fmt.Errorf("gzip decompress error on %s: %w", name, err)
			}
		}
		gzipDecompressDuration = time.Since(startDecomp) / time.Duration(iterations)

		gzipRatio := float64(len(gzipCompressed)) / float64(origSize)
		results = append(results, BenchmarkResult{
			PayloadName:        name,
			Algorithm:          "gzip",
			OriginalBytes:      origSize,
			CompressedBytes:    len(gzipCompressed),
			SavingsPercent:     (1.0 - gzipRatio) * 100.0,
			CompressionRatio:   gzipRatio,
			CompressDuration:   gzipCompressDuration,
			DecompressDuration: gzipDecompressDuration,
		})

		// 2. Benchmark Zstd
		var zstdCompressed []byte
		var zstdCompressDuration, zstdDecompressDuration time.Duration

		startCompZ := time.Now()
		for i := 0; i < iterations; i++ {
			var err error
			zstdCompressed, err = CompressZstd(data)
			if err != nil {
				return nil, fmt.Errorf("zstd benchmark error on %s: %w", name, err)
			}
		}
		zstdCompressDuration = time.Since(startCompZ) / time.Duration(iterations)

		startDecompZ := time.Now()
		for i := 0; i < iterations; i++ {
			if _, err := DecompressZstd(zstdCompressed); err != nil {
				return nil, fmt.Errorf("zstd decompress error on %s: %w", name, err)
			}
		}
		zstdDecompressDuration = time.Since(startDecompZ) / time.Duration(iterations)

		zstdRatio := float64(len(zstdCompressed)) / float64(origSize)
		results = append(results, BenchmarkResult{
			PayloadName:        name,
			Algorithm:          "zstd",
			OriginalBytes:      origSize,
			CompressedBytes:    len(zstdCompressed),
			SavingsPercent:     (1.0 - zstdRatio) * 100.0,
			CompressionRatio:   zstdRatio,
			CompressDuration:   zstdCompressDuration,
			DecompressDuration: zstdDecompressDuration,
		})
	}

	return results, nil
}

// GenerateRealisticPayloads generates small, medium, and batch Tarbook maritime sync payloads.
func GenerateRealisticPayloads() map[string][]byte {
	// 1. Single Task Entry
	taskEntry, _ := json.Marshal(map[string]interface{}{
		"operation_id": uuid.New().String(),
		"entity_type":  "task_entries",
		"action":       "INSERT",
		"payload": map[string]interface{}{
			"tar_book_id":        uuid.New().String(),
			"task_definition_id": uuid.New().String(),
			"status":             "SUBMITTED",
			"candidate_notes":    "Conducted pre-departure safety briefing with deck crew. Inspected anchor windlass brake lining and hydraulic piping.",
			"vessel_imo":         "9876543",
			"logged_at_utc":      time.Now().UTC().Format(time.RFC3339),
		},
	})

	// 2. Officer Sign-Off with signature
	signoff, _ := json.Marshal(map[string]interface{}{
		"operation_id": uuid.New().String(),
		"entity_type":  "task_signoffs",
		"action":       "INSERT",
		"payload": map[string]interface{}{
			"task_entry_id":          uuid.New().String(),
			"signer_role":            "SUPERVISING_OFFICER",
			"verdict":                "COMPETENT",
			"comments":               "Candidate demonstrated proficiency in passage planning, ARPA plotting, and collision regulation application under night navigation conditions.",
			"public_key_fingerprint": "SHA256:4f8a3c1e9b7d2e0f5a6b8c7d1e3f5a7b9c1d3e5f7a9b1c3d5e7f9a1b3c5d7e9f",
			"signature_bytes":        "MEQCIFe4y7h2J4W9T5v8K3m1L7r0P9s6D2n4X8z1V5t9C7q3AiB6u8J2L4W0T8v5K1m7R3p9S5d2N6x0Z4v8T2c6Q==",
			"signed_at_utc":          time.Now().UTC().Format(time.RFC3339),
		},
	})

	// 3. Realistic Batch of 15 Operations
	var ops []map[string]interface{}
	for i := 0; i < 15; i++ {
		ops = append(ops, map[string]interface{}{
			"operation_id": uuid.New().String(),
			"entity_type":  "task_entries",
			"action":       "INSERT",
			"payload": map[string]interface{}{
				"title":           fmt.Sprintf("Maritime Competency Task #%03d", i+1),
				"vessel_imo":      "9876543",
				"candidate_notes": "Executed auxiliary generator synchronization and load sharing sequence per SOLAS Chapter II-1 regulation standards.",
				"latitude":        1.283333,
				"longitude":       103.833333,
				"timestamp":       time.Now().UTC().Format(time.RFC3339),
			},
		})
	}
	batch, _ := json.Marshal(map[string]interface{}{
		"sync_session_id": uuid.New().String(),
		"client_id":       "vessel-sim-9876543",
		"operations":      ops,
	})

	return map[string][]byte{
		"1_small_task_entry":  taskEntry,
		"2_medium_signoff":    signoff,
		"3_large_sync_batch":  batch,
	}
}
