package com.mralmostcool.tarbook.certificate.dto;

import com.mralmostcool.tarbook.certificate.internal.domain.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeafarerCertificateRequestDto {
    private UUID candidateId;
    private CertificateType certificateType;
    private String certificateNumber;
    private String issuingMtiName;
    private String issuingMtiCode;
    private LocalDate issueDate;
    private LocalDate expiryDate;
}
