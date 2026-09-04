package com.mralmostcool.tarbook.security.internal.service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class AndroidKeyAttestationValidator {

    public boolean validateCertChainAndChallenge(List<String> pemCertChain, String expectedChallengeNonce) {
        if (pemCertChain == null || pemCertChain.isEmpty()) {
            return false;
        }
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<X509Certificate> certs = new ArrayList<>();
            for (String pem : pemCertChain) {
                byte[] decoded = Base64.getDecoder().decode(pem.replace("-----BEGIN CERTIFICATE-----", "")
                        .replace("-----END CERTIFICATE-----", "")
                        .replaceAll("\\s", ""));
                X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(decoded));
                certs.add(cert);
            }
            // Basic sanity check on certificate chain presence
            return !certs.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
