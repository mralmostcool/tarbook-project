package com.mralmostcool.tarbook.security;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.security.dto.AttestationChallengeDto;
import com.mralmostcool.tarbook.security.dto.KeyEnrollmentRequestDto;
import com.mralmostcool.tarbook.security.dto.SigningKeyDto;
import com.mralmostcool.tarbook.security.internal.domain.KeyStatus;
import com.mralmostcool.tarbook.security.internal.domain.OfficerSigningKey;
import com.mralmostcool.tarbook.security.internal.service.AndroidKeyAttestationValidator;
import com.mralmostcool.tarbook.security.internal.service.AppleAppAttestValidator;
import com.mralmostcool.tarbook.security.internal.service.OfficerKeyGovernanceInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final OfficerKeyGovernanceInternalService keyGovernanceService;
    private final AppUserInternalService userInternalService;
    private final AndroidKeyAttestationValidator androidValidator;
    private final AppleAppAttestValidator appleValidator;

    public AttestationChallengeDto generateAttestationChallenge() {
        return AttestationChallengeDto.builder()
                .challengeNonce(UUID.randomUUID().toString())
                .expiresAtUtc(OffsetDateTime.now().plusMinutes(10))
                .build();
    }

    @Transactional
    public SigningKeyDto enrollOfficerKey(KeyEnrollmentRequestDto request) {
        AppUser officer = userInternalService.findById(request.getOfficerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Officer user not found: " + request.getOfficerUserId()));

        boolean validAttestation = false;
        if ("ANDROID".equalsIgnoreCase(request.getPlatform())) {
            validAttestation = androidValidator.validateCertChainAndChallenge(request.getPemCertChain(), request.getChallengeNonce());
        } else if ("IOS".equalsIgnoreCase(request.getPlatform())) {
            validAttestation = appleValidator.validateAppAttestStatement(request.getAttestationStatement(), request.getChallengeNonce());
        }

        if (!validAttestation) {
            throw new IllegalArgumentException("Hardware key attestation verification failed for platform: " + request.getPlatform());
        }

        OfficerSigningKey key = keyGovernanceService.enrollKey(
                officer,
                request.getKeyId(),
                request.getPublicKeyPem(),
                request.getAttestationStatement() != null ? request.getAttestationStatement() : String.join("\n", request.getPemCertChain())
        );

        return mapToDto(key);
    }

    @Transactional(readOnly = true)
    public Optional<SigningKeyDto> getKeyByKeyId(String keyId) {
        return keyGovernanceService.findByKeyId(keyId).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<SigningKeyDto> getActiveRevocationList() {
        return keyGovernanceService.findByStatus(KeyStatus.REVOKED).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private SigningKeyDto mapToDto(OfficerSigningKey key) {
        return SigningKeyDto.builder()
                .id(key.getId())
                .officerUserId(key.getOfficerUser().getId())
                .keyId(key.getKeyId())
                .publicKeyPem(key.getPublicKeyPem())
                .algorithm(key.getAlgorithm())
                .hardwareBacked(key.isHardwareBacked())
                .status(key.getStatus())
                .approvedByOrgId(key.getApprovedByOrganization() != null ? key.getApprovedByOrganization().getId() : null)
                .activatedAtUtc(key.getActivatedAtUtc())
                .revokedAtUtc(key.getRevokedAtUtc())
                .revocationReason(key.getRevocationReason())
                .createdAtUtc(key.getCreatedAtUtc())
                .build();
    }
}
