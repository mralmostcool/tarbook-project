package com.mralmostcool.tarbook.certificate.internal.domain;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_verification_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVerificationRecord {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_entity_type", nullable = false, length = 50)
    private TargetEntityType targetEntityType;

    @Column(name = "target_entity_id", nullable = false)
    private UUID targetEntityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verifier_user_id", nullable = false)
    private AppUser verifierUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verifier_org_id", nullable = false)
    private Organization verifierOrg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VerificationDecision decision;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "evidence_artifact_id")
    private UUID evidenceArtifactId;

    @Column(name = "evidence_digest_sha256", nullable = false, length = 64)
    private String evidenceDigestSha256;

    @Column(name = "canonical_payload_jcs", nullable = false, columnDefinition = "TEXT")
    private String canonicalPayloadJcs;

    @Column(name = "verified_at_utc", nullable = false)
    private OffsetDateTime verifiedAtUtc;

    @Column(name = "created_at_utc", nullable = false, updatable = false)
    private OffsetDateTime createdAtUtc;
}
