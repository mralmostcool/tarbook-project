package compression

import (
	"bytes"
	"testing"
)

func TestCompression_RoundTripGzip(t *testing.T) {
	original := []byte("Project Tarbook: Maritime Training Record Book with STCW Compliance. " +
		"Testing repeated strings for realistic compression ratio calculation.")

	compressed, err := CompressGzip(original)
	if err != nil {
		t.Fatalf("gzip compression failed: %v", err)
	}

	if len(compressed) == 0 {
		t.Fatalf("compressed data is empty")
	}

	decompressed, err := DecompressGzip(compressed)
	if err != nil {
		t.Fatalf("gzip decompression failed: %v", err)
	}

	if !bytes.Equal(original, decompressed) {
		t.Fatalf("decompressed data mismatch")
	}
}

func TestCompression_RoundTripZstd(t *testing.T) {
	original := []byte("Project Tarbook: Maritime Training Record Book with STCW Compliance. " +
		"Testing repeated strings for realistic compression ratio calculation.")

	compressed, err := CompressZstd(original)
	if err != nil {
		t.Fatalf("zstd compression failed: %v", err)
	}

	if len(compressed) == 0 {
		t.Fatalf("compressed data is empty")
	}

	decompressed, err := DecompressZstd(compressed)
	if err != nil {
		t.Fatalf("zstd decompression failed: %v", err)
	}

	if !bytes.Equal(original, decompressed) {
		t.Fatalf("decompressed data mismatch")
	}
}

func TestCompression_BenchmarkHarness(t *testing.T) {
	payloads := map[string][]byte{
		"small_task": []byte(`{"title":"Bridge Watch","vessel_imo":"IMO9876543","notes":"Clear weather"}`),
		"signoff_batch": []byte(`{"operations":[{"operation_id":"op-1","entity_type":"task_signoffs","action":"INSERT","payload":{"verdict":"COMPETENT","comments":"Satisfactory execution of ECDIS passage planning and collision avoidance maneuvers under supervision."}}]}`),
	}

	results, err := RunBenchmark(payloads, 5)
	if err != nil {
		t.Fatalf("benchmark failed: %v", err)
	}

	if len(results) != len(payloads)*2 { // gzip and zstd per payload
		t.Fatalf("expected %d results, got %d", len(payloads)*2, len(results))
	}

	for _, res := range results {
		if res.OriginalBytes == 0 || res.CompressedBytes == 0 {
			t.Fatalf("invalid byte counts in result: %+v", res)
		}
		if res.CompressionRatio <= 0 {
			t.Fatalf("unexpected non-positive compression ratio: %f", res.CompressionRatio)
		}
	}
}
