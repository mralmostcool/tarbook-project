package com.mralmostcool.tarbook.journal.dto;

import com.mralmostcool.tarbook.journal.internal.domain.EvidenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceArtifactDto {
    private UUID id;
    private UUID journalEntryId;
    private UUID officerSigningKeyId;
    private EvidenceType artifactType;
    private String contentHash;
    private String officerSignature;
    private String storageKey;
    private OffsetDateTime capturedAtUtc;
}
