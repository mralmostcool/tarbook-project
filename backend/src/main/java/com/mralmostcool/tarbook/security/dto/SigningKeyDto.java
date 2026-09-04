package com.mralmostcool.tarbook.security.dto;

import com.mralmostcool.tarbook.security.internal.domain.KeyStatus;
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
public class SigningKeyDto {
    private UUID id;
    private UUID officerUserId;
    private String keyId;
    private String publicKeyPem;
    private String algorithm;
    private boolean hardwareBacked;
    private KeyStatus status;
    private UUID approvedByOrgId;
    private OffsetDateTime activatedAtUtc;
    private OffsetDateTime revokedAtUtc;
    private String revocationReason;
    private OffsetDateTime createdAtUtc;
}
