package com.mralmostcool.tarbook.certificate.dto;

import com.mralmostcool.tarbook.certificate.internal.domain.CertificateType;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeafarerCertificateDto {
    private UUID id;
    private UUID candidateId;
    private CertificateType certificateType;
    private String certificateNumber;
    private String issuingMtiName;
    private String issuingMtiCode;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private DocumentStatus status;
    private UUID replacedByCertificateId;
    private OffsetDateTime createdAtUtc;
    private OffsetDateTime updatedAtUtc;
}
