package com.mralmostcool.tarbook.security.internal.service;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.Organization;
import com.mralmostcool.tarbook.security.internal.domain.KeyStatus;
import com.mralmostcool.tarbook.security.internal.domain.OfficerSigningKey;
import com.mralmostcool.tarbook.security.internal.repository.OfficerSigningKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfficerKeyGovernanceInternalService {

    private final OfficerSigningKeyRepository keyRepository;

    @Transactional(readOnly = true)
    public Optional<OfficerSigningKey> findByKeyId(String keyId) {
        return keyRepository.findByKeyId(keyId);
    }

    @Transactional(readOnly = true)
    public List<OfficerSigningKey> findByOfficerUserId(UUID officerUserId) {
        return keyRepository.findByOfficerUserId(officerUserId);
    }

    @Transactional(readOnly = true)
    public List<OfficerSigningKey> findByStatus(KeyStatus status) {
        return keyRepository.findByStatus(status);
    }

    @Transactional
    public OfficerSigningKey enrollKey(AppUser officerUser, String keyId, String publicKeyPem, String attestationStatement) {
        OfficerSigningKey key = OfficerSigningKey.builder()
                .id(UUID.randomUUID())
                .officerUser(officerUser)
                .keyId(keyId)
                .publicKeyPem(publicKeyPem)
                .algorithm("ECDSA_P256")
                .hardwareBacked(true)
                .attestationStatement(attestationStatement)
                .status(KeyStatus.PENDING_APPROVAL)
                .createdAtUtc(OffsetDateTime.now())
                .build();
        return keyRepository.save(key);
    }

    @Transactional
    public OfficerSigningKey approveKey(UUID keyIdUuid, Organization org) {
        OfficerSigningKey key = keyRepository.findById(keyIdUuid)
                .orElseThrow(() -> new IllegalArgumentException("Signing key not found: " + keyIdUuid));
        key.setStatus(KeyStatus.ACTIVE);
        key.setApprovedByOrganization(org);
        key.setActivatedAtUtc(OffsetDateTime.now());
        return keyRepository.save(key);
    }

    @Transactional
    public OfficerSigningKey revokeKey(UUID keyIdUuid, String reason) {
        OfficerSigningKey key = keyRepository.findById(keyIdUuid)
                .orElseThrow(() -> new IllegalArgumentException("Signing key not found: " + keyIdUuid));
        key.setStatus(KeyStatus.REVOKED);
        key.setRevocationReason(reason);
        key.setRevokedAtUtc(OffsetDateTime.now());
        return keyRepository.save(key);
    }
}
