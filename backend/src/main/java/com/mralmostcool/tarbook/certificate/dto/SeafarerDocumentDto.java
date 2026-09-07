package com.mralmostcool.tarbook.certificate.dto;

import com.mralmostcool.tarbook.certificate.internal.domain.DocumentStatus;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentType;
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
public class SeafarerDocumentDto {
    private UUID id;
    private UUID candidateId;
    private DocumentType documentType;
    private String documentNumber;
    private String issuingCountryCode;
    private String issuingAuthority;
    private String placeOfIssue;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private DocumentStatus status;
    private OffsetDateTime createdAtUtc;
    private OffsetDateTime updatedAtUtc;
}
