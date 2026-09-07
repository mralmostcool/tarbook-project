package com.mralmostcool.tarbook.certificate.internal.domain;

import com.mralmostcool.tarbook.core.internal.domain.Candidate;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "seafarer_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeafarerCertificate {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false, length = 50)
    private CertificateType certificateType;

    @Column(name = "certificate_number", nullable = false, length = 100)
    private String certificateNumber;

    @Column(name = "issuing_mti_name", nullable = false, length = 255)
    private String issuingMtiName;

    @Column(name = "issuing_mti_code", length = 100)
    private String issuingMtiCode;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentStatus status;

    @Column(name = "replaced_by_certificate_id")
    private UUID replacedByCertificateId;

    @Column(name = "created_at_utc", nullable = false, updatable = false)
    private OffsetDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false)
    private OffsetDateTime updatedAtUtc;
}
