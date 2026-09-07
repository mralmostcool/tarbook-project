package com.mralmostcool.tarbook.seaservice.internal.domain;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
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
@Table(name = "sea_service_endorsements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeaServiceEndorsement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sea_service_id", nullable = false)
    private SeaServiceRecord seaServiceRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "endorser_user_id", nullable = false)
    private AppUser endorserUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "endorser_role", nullable = false, length = 50)
    private EndorserRole endorserRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "endorsement_type", nullable = false, length = 50)
    private EndorsementType endorsementType;

    @Column(name = "conduct_rating", nullable = false, length = 50)
    private String conductRating;

    @Column(name = "ability_rating", nullable = false, length = 50)
    private String abilityRating;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "key_id", nullable = false, length = 100)
    private String keyId;

    @Column(name = "signing_nonce", nullable = false)
    private UUID signingNonce;

    @Column(name = "signature_payload_hash", nullable = false, length = 64)
    private String signaturePayloadHash;

    @Column(name = "signature_bytes", nullable = false)
    private byte[] signatureBytes;

    @Column(name = "signed_at_utc", nullable = false)
    private OffsetDateTime signedAtUtc;

    @Column(name = "created_at_utc", nullable = false, updatable = false)
    private OffsetDateTime createdAtUtc;
}
