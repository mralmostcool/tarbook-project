package com.mralmostcool.tarbook.certificate.dto;

import com.mralmostcool.tarbook.certificate.internal.domain.TargetEntityType;
import com.mralmostcool.tarbook.certificate.internal.domain.VerificationDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDocumentRequestDto {
    private TargetEntityType targetEntityType;
    private UUID targetEntityId;
    private UUID verifierUserId;
    private UUID verifierOrgId;
    private VerificationDecision decision;
    private String decisionReason;
    private UUID evidenceArtifactId;
    private String rawEvidencePayload;
}
