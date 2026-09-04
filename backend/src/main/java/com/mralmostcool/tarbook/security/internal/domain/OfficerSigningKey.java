package com.mralmostcool.tarbook.security.internal.domain;

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
@Table(name = "officer_signing_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerSigningKey {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_user_id", nullable = false)
    private AppUser officerUser;

    @Column(name = "key_id", nullable = false, unique = true, length = 100)
    private String keyId;

    @Column(name = "public_key_pem", nullable = false, columnDefinition = "TEXT")
    private String publicKeyPem;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String algorithm = "ECDSA_P256";

    @Column(name = "hardware_backed", nullable = false)
    @Builder.Default
    private boolean hardwareBacked = true;

    @Column(name = "attestation_statement", columnDefinition = "TEXT")
    private String attestationStatement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private KeyStatus status = KeyStatus.PENDING_APPROVAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_org_id")
    private Organization approvedByOrganization;

    @Column(name = "activated_at_utc")
    private OffsetDateTime activatedAtUtc;

    @Column(name = "revoked_at_utc")
    private OffsetDateTime revokedAtUtc;

    @Column(name = "revocation_reason")
    private String revocationReason;

    @Column(name = "created_at_utc", nullable = false, updatable = false)
    private OffsetDateTime createdAtUtc;
}
