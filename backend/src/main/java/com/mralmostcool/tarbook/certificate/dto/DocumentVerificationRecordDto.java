package com.mralmostcool.tarbook.certificate.dto;

import com.mralmostcool.tarbook.certificate.internal.domain.TargetEntityType;
import com.mralmostcool.tarbook.certificate.internal.domain.VerificationDecision;
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
public class DocumentVerificationRecordDto {
    private UUID id;
    private TargetEntityType targetEntityType;
    private UUID targetEntityId;
    private UUID verifierUserId;
    private String verifierUserName;
    private UUID verifierOrgId;
    private String verifierOrgName;
    private VerificationDecision decision;
    private String decisionReason;
    private UUID evidenceArtifactId;
    private String evidenceDigestSha256;
    private String canonicalPayloadJcs;
    private OffsetDateTime verifiedAtUtc;
    private OffsetDateTime createdAtUtc;
}
