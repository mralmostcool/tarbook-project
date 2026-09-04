package evidence

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/google/uuid"
)

type mockObjectUploader struct {
	uploadedURL     string
	uploadedHeaders map[string]string
	uploadedBytes   []byte
	uploadErr       error
}

func (m *mockObjectUploader) UploadObject(ctx context.Context, uploadURL string, headers map[string]string, content io.Reader, size int64) error {
	if m.uploadErr != nil {
		return m.uploadErr
	}
	m.uploadedURL = uploadURL
	m.uploadedHeaders = headers
	data, err := io.ReadAll(content)
	if err != nil {
		return err
	}
	m.uploadedBytes = data
	return nil
}

type mockAuthRefresher struct {
	refreshedID   uuid.UUID
	freshAuth     *UploadAuthorization
	refreshErr    error
	refreshCalled int
}

func (m *mockAuthRefresher) RefreshUploadAuthorization(ctx context.Context, artifactID uuid.UUID) (*UploadAuthorization, error) {
	m.refreshCalled++
	m.refreshedID = artifactID
	if m.refreshErr != nil {
		return nil, m.refreshErr
	}
	return m.freshAuth, nil
}

type mockCompletionReporter struct {
	reportedID       uuid.UUID
	reportedChecksum string
	result           *VerificationResult
	reportErr        error
	reportCalled     int
}

func (m *mockCompletionReporter) ReportUploadComplete(ctx context.Context, artifactID uuid.UUID, sha256Checksum string) (*VerificationResult, error) {
	m.reportCalled++
	m.reportedID = artifactID
	m.reportedChecksum = sha256Checksum
	if m.reportErr != nil {
		return nil, m.reportErr
	}
	return m.result, nil
}

func TestEvidenceUploader_Success_ValidAuthorizationAndUpload(t *testing.T) {
	ctx := context.Background()
	artifactID := uuid.New()
	taskEntryID := uuid.New()

	payload := []byte("fake binary evidence image data")
	h := sha256.Sum256(payload)
	checksum := hex.EncodeToString(h[:])

	evidence := LocalEvidence{
		ID:             artifactID,
		TaskEntryID:    taskEntryID,
		FileName:       "bridge_radar_log.jpg",
		MimeType:       "image/jpeg",
		FileSizeBytes:  int64(len(payload)),
		SHA256Checksum: checksum,
		Status:         StatusPendingUpload,
	}

	auth := &UploadAuthorization{
		ArtifactID: artifactID,
		UploadURL:  "https://minio.ship.local/evidence-bucket/artifacts/123",
		Headers: map[string]string{
			"Content-Type": "image/jpeg",
		},
		ExpiresAt: time.Now().Add(10 * time.Minute),
	}

	objUploader := &mockObjectUploader{}
	refresher := &mockAuthRefresher{}
	reporter := &mockCompletionReporter{
		result: &VerificationResult{
			ArtifactID: artifactID,
			Status:     StatusVerified,
		},
	}

	service := NewEvidenceUploader(objUploader, refresher, reporter)
	report, err := service.Upload(ctx, evidence, auth, bytes.NewReader(payload))
	if err != nil {
		t.Fatalf("unexpected upload error: %v", err)
	}

	if report.Status != StatusVerified {
		t.Fatalf("expected status %s, got %s", StatusVerified, report.Status)
	}
	if !report.Verified {
		t.Fatalf("expected report.Verified to be true")
	}
	if refresher.refreshCalled != 0 {
		t.Fatalf("expected refresh not to be called for valid auth")
	}
	if reporter.reportCalled != 1 {
		t.Fatalf("expected completion reporter to be called once")
	}
	if string(objUploader.uploadedBytes) != string(payload) {
		t.Fatalf("uploaded bytes mismatch")
	}
}

func TestEvidenceUploader_ExpiredAuthorization_RefreshesAndUploads(t *testing.T) {
	ctx := context.Background()
	artifactID := uuid.New()
	payload := []byte("steering test evidence")
	h := sha256.Sum256(payload)
	checksum := hex.EncodeToString(h[:])

	evidence := LocalEvidence{
		ID:             artifactID,
		FileName:       "steering.png",
		MimeType:       "image/png",
		FileSizeBytes:  int64(len(payload)),
		SHA256Checksum: checksum,
		Status:         StatusPendingUpload,
	}

	expiredAuth := &UploadAuthorization{
		ArtifactID: artifactID,
		UploadURL:  "https://minio.ship.local/expired",
		ExpiresAt:  time.Now().Add(-5 * time.Minute), // expired
	}

	freshAuth := &UploadAuthorization{
		ArtifactID: artifactID,
		UploadURL:  "https://minio.ship.local/fresh",
		ExpiresAt:  time.Now().Add(15 * time.Minute),
	}

	objUploader := &mockObjectUploader{}
	refresher := &mockAuthRefresher{freshAuth: freshAuth}
	reporter := &mockCompletionReporter{
		result: &VerificationResult{
			ArtifactID: artifactID,
			Status:     StatusVerified,
		},
	}

	service := NewEvidenceUploader(objUploader, refresher, reporter)
	report, err := service.Upload(ctx, evidence, expiredAuth, bytes.NewReader(payload))
	if err != nil {
		t.Fatalf("unexpected upload error: %v", err)
	}

	if refresher.refreshCalled != 1 {
		t.Fatalf("expected refresh to be called once for expired auth")
	}
	if !report.RefreshedURL {
		t.Fatalf("expected RefreshedURL to be true")
	}
	if objUploader.uploadedURL != "https://minio.ship.local/fresh" {
		t.Fatalf("expected upload to fresh URL, got %s", objUploader.uploadedURL)
	}
	if report.Status != StatusVerified {
		t.Fatalf("expected status VERIFIED, got %s", report.Status)
	}
}

func TestEvidenceUploader_BinaryUploadFailure_PreservesLocalState(t *testing.T) {
	ctx := context.Background()
	artifactID := uuid.New()
	payload := []byte("engine room log")
	h := sha256.Sum256(payload)
	checksum := hex.EncodeToString(h[:])

	evidence := LocalEvidence{
		ID:             artifactID,
		FileName:       "engine.jpg",
		MimeType:       "image/jpeg",
		FileSizeBytes:  int64(len(payload)),
		SHA256Checksum: checksum,
		Status:         StatusPendingUpload,
	}

	auth := &UploadAuthorization{
		ArtifactID: artifactID,
		UploadURL:  "https://minio.ship.local/evidence-bucket/artifacts/456",
		ExpiresAt:  time.Now().Add(10 * time.Minute),
	}

	objUploader := &mockObjectUploader{
		uploadErr: errors.New("satellite connection dropped"),
	}
	refresher := &mockAuthRefresher{}
	reporter := &mockCompletionReporter{}

	service := NewEvidenceUploader(objUploader, refresher, reporter)
	report, err := service.Upload(ctx, evidence, auth, bytes.NewReader(payload))
	if err == nil {
		t.Fatalf("expected error on binary upload failure, got nil")
	}

	if report.Status != StatusUploadFailed {
		t.Fatalf("expected report status %s, got %s", StatusUploadFailed, report.Status)
	}
	// Invariant: Expected checksum and metadata must NOT be altered
	if evidence.SHA256Checksum != checksum {
		t.Fatalf("expected checksum %s to be preserved, got %s", checksum, evidence.SHA256Checksum)
	}
	if reporter.reportCalled != 0 {
		t.Fatalf("completion reporter must not be called when binary upload fails")
	}
}

func TestEvidenceUploader_ChecksumMismatch_RefusesUploadAndPreservesMetadata(t *testing.T) {
	ctx := context.Background()
	artifactID := uuid.New()
	payload := []byte("actual binary content")

	// Pre-recorded metadata has different checksum
	expectedChecksum := "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

	evidence := LocalEvidence{
		ID:             artifactID,
		FileName:       "tampered_file.pdf",
		MimeType:       "application/pdf",
		FileSizeBytes:  int64(len(payload)),
		SHA256Checksum: expectedChecksum,
		Status:         StatusPendingUpload,
	}

	auth := &UploadAuthorization{
		ArtifactID: artifactID,
		UploadURL:  "https://minio.ship.local/evidence-bucket/artifacts/789",
		ExpiresAt:  time.Now().Add(10 * time.Minute),
	}

	objUploader := &mockObjectUploader{}
	refresher := &mockAuthRefresher{}
	reporter := &mockCompletionReporter{}

	service := NewEvidenceUploader(objUploader, refresher, reporter)
	report, err := service.Upload(ctx, evidence, auth, bytes.NewReader(payload))
	if !errors.Is(err, ErrChecksumMismatch) {
		t.Fatalf("expected ErrChecksumMismatch, got: %v", err)
	}

	if objUploader.uploadedURL != "" {
		t.Fatalf("uploader must not send data to S3 when checksum mismatches")
	}
	if evidence.SHA256Checksum != expectedChecksum {
		t.Fatalf("expected original checksum to be preserved without alteration")
	}
	if report.Status != StatusUploadFailed {
		t.Fatalf("expected status %s, got %s", StatusUploadFailed, report.Status)
	}
}

func TestEvidenceUploader_CompletionFailure_RecordsUploadedState(t *testing.T) {
	ctx := context.Background()
	artifactID := uuid.New()
	payload := []byte("generator checklist")
	h := sha256.Sum256(payload)
	checksum := hex.EncodeToString(h[:])

	evidence := LocalEvidence{
		ID:             artifactID,
		FileName:       "gen.png",
		MimeType:       "image/png",
		FileSizeBytes:  int64(len(payload)),
		SHA256Checksum: checksum,
		Status:         StatusPendingUpload,
	}

	auth := &UploadAuthorization{
		ArtifactID: artifactID,
		UploadURL:  "https://minio.ship.local/evidence-bucket/artifacts/999",
		ExpiresAt:  time.Now().Add(10 * time.Minute),
	}

	objUploader := &mockObjectUploader{}
	refresher := &mockAuthRefresher{}
	reporter := &mockCompletionReporter{
		reportErr: errors.New("backend timeout verifying upload"),
	}

	service := NewEvidenceUploader(objUploader, refresher, reporter)
	report, err := service.Upload(ctx, evidence, auth, bytes.NewReader(payload))
	if err == nil {
		t.Fatalf("expected error when completion report fails, got nil")
	}

	// Invariant: Binary upload succeeded, but verification pending
	if report.Status != StatusUploaded {
		t.Fatalf("expected status %s when binary uploaded but reporter failed, got %s", StatusUploaded, report.Status)
	}
	if report.Verified {
		t.Fatalf("report must not be verified when completion reporter errors")
	}
}

func TestEvidenceUploader_ServerVerificationFailed(t *testing.T) {
	ctx := context.Background()
	artifactID := uuid.New()
	payload := []byte("damaged payload")
	h := sha256.Sum256(payload)
	checksum := hex.EncodeToString(h[:])

	evidence := LocalEvidence{
		ID:             artifactID,
		FileName:       "damaged.jpg",
		MimeType:       "image/jpeg",
		FileSizeBytes:  int64(len(payload)),
		SHA256Checksum: checksum,
		Status:         StatusPendingUpload,
	}

	auth := &UploadAuthorization{
		ArtifactID: artifactID,
		UploadURL:  "https://minio.ship.local/evidence-bucket/artifacts/000",
		ExpiresAt:  time.Now().Add(10 * time.Minute),
	}

	objUploader := &mockObjectUploader{}
	refresher := &mockAuthRefresher{}
	reporter := &mockCompletionReporter{
		result: &VerificationResult{
			ArtifactID: artifactID,
			Status:     StatusFailedVerification,
			Message:    "checksum verified but image file signature corrupt",
		},
	}

	service := NewEvidenceUploader(objUploader, refresher, reporter)
	report, err := service.Upload(ctx, evidence, auth, bytes.NewReader(payload))
	if err != nil {
		t.Fatalf("unexpected upload orchestration error: %v", err)
	}

	if report.Status != StatusFailedVerification {
		t.Fatalf("expected status %s, got %s", StatusFailedVerification, report.Status)
	}
	if report.Verified {
		t.Fatalf("expected report.Verified to be false")
	}
}

func TestHTTPObjectUploader_SuccessAndFailures(t *testing.T) {
	ctx := context.Background()
	content := []byte("binary stream to S3")

	// 1. Success (HTTP 200)
	serverOK := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPut {
			t.Fatalf("expected method PUT, got %s", r.Method)
		}
		if r.Header.Get("X-Custom-Header") != "present" {
			t.Fatalf("missing custom header")
		}
		data, _ := io.ReadAll(r.Body)
		if string(data) != string(content) {
			t.Fatalf("payload body mismatch")
		}
		w.WriteHeader(http.StatusOK)
	}))
	defer serverOK.Close()

	uploader := NewHTTPObjectUploader(serverOK.Client())
	err := uploader.UploadObject(ctx, serverOK.URL, map[string]string{"X-Custom-Header": "present"}, bytes.NewReader(content), int64(len(content)))
	if err != nil {
		t.Fatalf("expected upload success, got %v", err)
	}

	// 2. HTTP 500 S3 failure
	server500 := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "internal S3 error", http.StatusInternalServerError)
	}))
	defer server500.Close()

	uploader500 := NewHTTPObjectUploader(server500.Client())
	err = uploader500.UploadObject(ctx, server500.URL, nil, bytes.NewReader(content), int64(len(content)))
	if err == nil {
		t.Fatalf("expected error on HTTP 500 from S3, got nil")
	}
}

