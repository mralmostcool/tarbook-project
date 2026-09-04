package com.mralmostcool.tarbook.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttestationChallengeDto {
    private String challengeNonce;
    private OffsetDateTime expiresAtUtc;
}
