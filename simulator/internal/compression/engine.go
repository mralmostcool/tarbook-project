package compression

import (
	"bytes"
	"fmt"
	"io"

	"github.com/klauspost/compress/gzip"
	"github.com/klauspost/compress/zstd"
)

var (
	zstdEncoder *zstd.Encoder
	zstdDecoder *zstd.Decoder
)

func init() {
	var err error
	zstdEncoder, err = zstd.NewWriter(nil)
	if err != nil {
		panic(fmt.Sprintf("failed to initialize zstd encoder: %v", err))
	}

	zstdDecoder, err = zstd.NewReader(nil)
	if err != nil {
		panic(fmt.Sprintf("failed to initialize zstd decoder: %v", err))
	}
}

// CompressGzip compresses input bytes using klauspost/compress/gzip at BestCompression level.
func CompressGzip(data []byte) ([]byte, error) {
	var buf bytes.Buffer
	w, err := gzip.NewWriterLevel(&buf, gzip.BestCompression)
	if err != nil {
		return nil, fmt.Errorf("failed to create gzip writer: %w", err)
	}

	if _, err := w.Write(data); err != nil {
		return nil, fmt.Errorf("failed to write gzip data: %w", err)
	}

	if err := w.Close(); err != nil {
		return nil, fmt.Errorf("failed to finalize gzip stream: %w", err)
	}

	return buf.Bytes(), nil
}

// DecompressGzip decompresses gzip encoded bytes.
func DecompressGzip(data []byte) ([]byte, error) {
	r, err := gzip.NewReader(bytes.NewReader(data))
	if err != nil {
		return nil, fmt.Errorf("failed to create gzip reader: %w", err)
	}
	defer r.Close()

	out, err := io.ReadAll(r)
	if err != nil {
		return nil, fmt.Errorf("failed to decompress gzip stream: %w", err)
	}

	return out, nil
}

// CompressZstd compresses input bytes using klauspost/compress/zstd.
func CompressZstd(data []byte) ([]byte, error) {
	return zstdEncoder.EncodeAll(data, make([]byte, 0, len(data))), nil
}

// DecompressZstd decompresses zstd encoded bytes.
func DecompressZstd(data []byte) ([]byte, error) {
	return zstdDecoder.DecodeAll(data, nil)
}
