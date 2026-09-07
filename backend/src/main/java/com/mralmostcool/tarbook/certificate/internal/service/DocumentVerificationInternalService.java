package com.mralmostcool.tarbook.certificate.internal.service;

import com.mralmostcool.tarbook.certificate.dto.DocumentVerificationRecordDto;
import com.mralmostcool.tarbook.certificate.dto.VerifyDocumentRequestDto;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentVerificationRecord;
import com.mralmostcool.tarbook.certificate.internal.domain.TargetEntityType;
import com.mralmostcool.tarbook.certificate.internal.repository.DocumentVerificationRecordRepository;
import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.Organization;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.core.internal.service.OrganizationInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentVerificationInternalService {

    private final DocumentVerificationRecordRepository verificationRepository;
    private final AppUserInternalService userInternalService;
    private final OrganizationInternalService orgInternalService;

    @Transactional
    public DocumentVerificationRecordDto verifyDocument(VerifyDocumentRequestDto request) {
        AppUser verifier = userInternalService.findById(request.getVerifierUserId())
                .orElseThrow(() -> new IllegalArgumentException("Verifier user not found: " + request.getVerifierUserId()));

        Organization verifierOrg = orgInternalService.findById(request.getVerifierOrgId())
                .orElseThrow(() -> new IllegalArgumentException("Verifier organization not found: " + request.getVerifierOrgId()));

        OffsetDateTime now = OffsetDateTime.now();
        String rawPayload = request.getRawEvidencePayload() != null ? request.getRawEvidencePayload() : "{}";
        String evidenceDigest = computeSha256(rawPayload);
        String jcsPayload = String.format("{\"decision\":\"%s\",\"targetEntityId\":\"%s\",\"targetEntityType\":\"%s\",\"verifierUserId\":\"%s\"}",
                request.getDecision(), request.getTargetEntityId(), request.getTargetEntityType(), request.getVerifierUserId());

        DocumentVerificationRecord record = DocumentVerificationRecord.builder()
                .id(UUID.randomUUID())
                .targetEntityType(request.getTargetEntityType())
                .targetEntityId(request.getTargetEntityId())
                .verifierUser(verifier)
                .verifierOrg(verifierOrg)
                .decision(request.getDecision())
                .decisionReason(request.getDecisionReason())
                .evidenceArtifactId(request.getEvidenceArtifactId())
                .evidenceDigestSha256(evidenceDigest)
                .canonicalPayloadJcs(jcsPayload)
                .verifiedAtUtc(now)
                .createdAtUtc(now)
                .build();

        DocumentVerificationRecord saved = verificationRepository.save(record);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentVerificationRecordDto> getVerificationsForTarget(TargetEntityType targetEntityType, UUID targetEntityId) {
        return verificationRepository.findByTargetEntityTypeAndTargetEntityIdOrderByVerifiedAtUtcAsc(targetEntityType, targetEntityId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public DocumentVerificationRecordDto mapToDto(DocumentVerificationRecord record) {
        return DocumentVerificationRecordDto.builder()
                .id(record.getId())
                .targetEntityType(record.getTargetEntityType())
                .targetEntityId(record.getTargetEntityId())
                .verifierUserId(record.getVerifierUser().getId())
                .verifierUserName(record.getVerifierUser().getFullName())
                .verifierOrgId(record.getVerifierOrg().getId())
                .verifierOrgName(record.getVerifierOrg().getName())
                .decision(record.getDecision())
                .decisionReason(record.getDecisionReason())
                .evidenceArtifactId(record.getEvidenceArtifactId())
                .evidenceDigestSha256(record.getEvidenceDigestSha256())
                .canonicalPayloadJcs(record.getCanonicalPayloadJcs())
                .verifiedAtUtc(record.getVerifiedAtUtc())
                .createdAtUtc(record.getCreatedAtUtc())
                .build();
    }

    private String computeSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }
}
