package com.mralmostcool.tarbook.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyEnrollmentRequestDto {
    private UUID officerUserId;
    private String keyId;
    private String publicKeyPem;
    private String platform; // ANDROID or IOS
    private List<String> pemCertChain; // For Android
    private String attestationStatement; // For iOS CBOR/X.509 statement
    private String challengeNonce;
}
