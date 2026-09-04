package evidence

import (
	"context"
	"errors"
	"io"
	"time"

	"github.com/google/uuid"
)

var (
	ErrChecksumMismatch     = errors.New("computed SHA-256 checksum does not match expected metadata checksum")
	ErrMissingAuthorization = errors.New("missing upload authorization")
)

type LocalEvidenceStatus string

const (
	StatusPendingUpload      LocalEvidenceStatus = "PENDING_UPLOAD"
	StatusUploading          LocalEvidenceStatus = "UPLOADING"
	StatusUploaded           LocalEvidenceStatus = "UPLOADED"
	StatusVerified           LocalEvidenceStatus = "VERIFIED"
	StatusFailedVerification LocalEvidenceStatus = "FAILED_VERIFICATION"
	StatusUploadFailed       LocalEvidenceStatus = "UPLOAD_FAILED"
)

// LocalEvidence represents the local edge record of an evidence artifact.
type LocalEvidence struct {
	ID              uuid.UUID           `json:"id"`
	TaskEntryID     uuid.UUID           `json:"task_entry_id"`
	FileName        string              `json:"file_name"`
	MimeType        string              `json:"mime_type"`
	FileSizeBytes   int64               `json:"file_size_bytes"`
	SHA256Checksum  string              `json:"sha256_checksum"`
	Status          LocalEvidenceStatus `json:"status"`
	LastUploadError string              `json:"last_upload_error,omitempty"`
}

// UploadAuthorization models the presigned PUT upload grant returned by Shore Sync.
type UploadAuthorization struct {
	ArtifactID uuid.UUID         `json:"artifact_id"`
	UploadURL  string            `json:"upload_url"`
	Headers    map[string]string `json:"headers,omitempty"`
	ExpiresAt  time.Time         `json:"expires_at"`
}

// IsExpired checks whether the upload authorization is no longer valid at the given time.
func (a *UploadAuthorization) IsExpired(now time.Time) bool {
	return !now.Before(a.ExpiresAt)
}

// VerificationResult contains the outcome of server-side evidence verification.
type VerificationResult struct {
	ArtifactID uuid.UUID           `json:"artifact_id"`
	Status     LocalEvidenceStatus `json:"status"`
	Message    string              `json:"message,omitempty"`
}

// UploadReport summarizes the outcome of the Phase 2 evidence upload.
type UploadReport struct {
	ArtifactID   uuid.UUID           `json:"artifact_id"`
	Status       LocalEvidenceStatus `json:"status"`
	Verified     bool                `json:"verified"`
	RefreshedURL bool                `json:"refreshed_url"`
	Error        error               `json:"error,omitempty"`
}

// ObjectUploader defines the low-level binary transfer seam to S3/MinIO.
type ObjectUploader interface {
	UploadObject(ctx context.Context, uploadURL string, headers map[string]string, content io.Reader, size int64) error
}

// AuthorizationRefresher defines the seam for refreshing an upload URL via Spring Boot.
type AuthorizationRefresher interface {
	RefreshUploadAuthorization(ctx context.Context, artifactID uuid.UUID) (*UploadAuthorization, error)
}

// CompletionReporter defines the seam for reporting upload completion and verifying checksum.
type CompletionReporter interface {
	ReportUploadComplete(ctx context.Context, artifactID uuid.UUID, sha256Checksum string) (*VerificationResult, error)
}

// EvidenceUploader defines the orchestrator seam for the Phase 2 upload workflow.
type EvidenceUploader interface {
	Upload(ctx context.Context, evidence LocalEvidence, auth *UploadAuthorization, content io.Reader) (*UploadReport, error)
}
