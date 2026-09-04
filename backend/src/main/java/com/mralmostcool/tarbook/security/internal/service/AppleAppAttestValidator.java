package com.mralmostcool.tarbook.security.internal.service;

import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class AppleAppAttestValidator {

    public boolean validateAppAttestStatement(String base64AttestationStatement, String expectedChallengeNonce) {
        if (base64AttestationStatement == null || base64AttestationStatement.isBlank()) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(base64AttestationStatement);
            return decoded.length > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
