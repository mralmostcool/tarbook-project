package com.mralmostcool.tarbook.security;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.SystemRole;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.security.dto.AttestationChallengeDto;
import com.mralmostcool.tarbook.security.dto.KeyEnrollmentRequestDto;
import com.mralmostcool.tarbook.security.dto.SigningKeyDto;
import com.mralmostcool.tarbook.security.internal.domain.KeyStatus;
import com.mralmostcool.tarbook.security.internal.domain.OfficerSigningKey;
import com.mralmostcool.tarbook.security.internal.service.AndroidKeyAttestationValidator;
import com.mralmostcool.tarbook.security.internal.service.AppleAppAttestValidator;
import com.mralmostcool.tarbook.security.internal.service.OfficerKeyGovernanceInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityServiceUnitTest {

    private SecurityService securityService;

    private UUID userId;
    private AppUser officerUser;
    private List<OfficerSigningKey> keyStore;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        officerUser = AppUser.builder()
                .id(userId)
                .email("officer@maritime.org")
                .fullName("Captain Smith")
                .systemRole(SystemRole.OFFICER)
                .build();

        keyStore = new ArrayList<>();

        AppUserInternalService userInternalService = new AppUserInternalService(null) {
            @Override
            public Optional<AppUser> findById(UUID id) {
                if (userId.equals(id)) {
                    return Optional.of(officerUser);
                }
                return Optional.empty();
            }
        };

        OfficerKeyGovernanceInternalService keyGovernanceService = new OfficerKeyGovernanceInternalService(null) {
            @Override
            public OfficerSigningKey enrollKey(AppUser user, String keyId, String publicKeyPem, String attestationStatement) {
                OfficerSigningKey key = OfficerSigningKey.builder()
                        .id(UUID.randomUUID())
                        .officerUser(user)
                        .keyId(keyId)
                        .publicKeyPem(publicKeyPem)
                        .status(KeyStatus.ACTIVE)
                        .hardwareBacked(true)
                        .activatedAtUtc(OffsetDateTime.now())
                        .build();
                keyStore.add(key);
                return key;
            }

            @Override
            public List<OfficerSigningKey> findByStatus(KeyStatus status) {
                return keyStore.stream().filter(k -> k.getStatus() == status).toList();
            }

            @Override
            public Optional<OfficerSigningKey> findByKeyId(String keyId) {
                return keyStore.stream().filter(k -> k.getKeyId().equals(keyId)).findFirst();
            }
        };

        AndroidKeyAttestationValidator androidValidator = new AndroidKeyAttestationValidator() {
            @Override
            public boolean validateCertChainAndChallenge(List<String> pemCertChain, String expectedChallengeNonce) {
                return pemCertChain != null && !pemCertChain.contains("FAIL");
            }
        };

        AppleAppAttestValidator appleValidator = new AppleAppAttestValidator() {
            @Override
            public boolean validateAppAttestStatement(String attestationStatement, String expectedChallengeNonce) {
                return attestationStatement != null && !attestationStatement.contains("FAIL");
            }
        };

        securityService = new SecurityService(
                keyGovernanceService,
                userInternalService,
                androidValidator,
                appleValidator
        );
    }

    @Test
    void shouldGenerateAttestationChallenge() {
        AttestationChallengeDto challenge = securityService.generateAttestationChallenge();

        assertThat(challenge).isNotNull();
        assertThat(challenge.getChallengeNonce()).isNotBlank();
        assertThat(challenge.getExpiresAtUtc()).isAfter(OffsetDateTime.now());
    }

    @Test
    void shouldEnrollAndroidOfficerKeySuccessfully() {
        KeyEnrollmentRequestDto req = KeyEnrollmentRequestDto.builder()
                .officerUserId(userId)
                .keyId("key-android-100")
                .publicKeyPem("PEM_PUB_KEY")
                .platform("ANDROID")
                .pemCertChain(List.of("PEM_CERT_OK"))
                .challengeNonce("nonce-123")
                .build();

        SigningKeyDto dto = securityService.enrollOfficerKey(req);

        assertThat(dto).isNotNull();
        assertThat(dto.getKeyId()).isEqualTo("key-android-100");
        assertThat(dto.getStatus()).isEqualTo(KeyStatus.ACTIVE);
    }

    @Test
    void shouldThrowExceptionWhenAttestationFails() {
        KeyEnrollmentRequestDto req = KeyEnrollmentRequestDto.builder()
                .officerUserId(userId)
                .keyId("key-invalid")
                .platform("ANDROID")
                .pemCertChain(List.of("FAIL"))
                .challengeNonce("nonce-123")
                .build();

        assertThatThrownBy(() -> securityService.enrollOfficerKey(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("attestation verification failed");
    }

    @Test
    void shouldReturnRevocationList() {
        OfficerSigningKey revokedKey = OfficerSigningKey.builder()
                .id(UUID.randomUUID())
                .officerUser(officerUser)
                .keyId("key-revoked-1")
                .status(KeyStatus.REVOKED)
                .revocationReason("Device compromised")
                .build();
        keyStore.add(revokedKey);

        List<SigningKeyDto> revocationList = securityService.getActiveRevocationList();

        assertThat(revocationList).hasSize(1);
        assertThat(revocationList.get(0).getKeyId()).isEqualTo("key-revoked-1");
        assertThat(revocationList.get(0).getRevocationReason()).isEqualTo("Device compromised");
    }
}
