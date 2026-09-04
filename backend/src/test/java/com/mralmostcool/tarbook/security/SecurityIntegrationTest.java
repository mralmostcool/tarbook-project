package com.mralmostcool.tarbook.security;

import com.mralmostcool.tarbook.TestcontainersConfiguration;
import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.SystemRole;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.security.dto.AttestationChallengeDto;
import com.mralmostcool.tarbook.security.dto.KeyEnrollmentRequestDto;
import com.mralmostcool.tarbook.security.dto.SigningKeyDto;
import com.mralmostcool.tarbook.security.internal.domain.KeyStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AppUserInternalService appUserInternalService;

    @Test
    void shouldGenerateAttestationChallenge() {
        AttestationChallengeDto challenge = securityService.generateAttestationChallenge();
        assertThat(challenge).isNotNull();
        assertThat(challenge.getChallengeNonce()).isNotBlank();
        assertThat(challenge.getExpiresAtUtc()).isNotNull();
    }

    @Test
    void shouldEnrollOfficerSigningKeyAndGetRevocationList() {
        AppUser officer = AppUser.builder()
                .id(UUID.randomUUID())
                .email("assessor.chief@maritime.org")
                .passwordHash("$2a$10$e83...")
                .fullName("Chief Assessor John")
                .systemRole(SystemRole.OFFICER)
                .build();
        appUserInternalService.save(officer);

        KeyEnrollmentRequestDto enrollmentReq = KeyEnrollmentRequestDto.builder()
                .officerUserId(officer.getId())
                .keyId("key-android-001")
                .publicKeyPem("-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...\n-----END PUBLIC KEY-----")
                .platform("ANDROID")
                .pemCertChain(List.of("-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"))
                .challengeNonce("test-challenge")
                .build();

        SigningKeyDto enrolledKey = securityService.enrollOfficerKey(enrollmentReq);
        assertThat(enrolledKey).isNotNull();
        assertThat(enrolledKey.getKeyId()).isEqualTo("key-android-001");
        assertThat(enrolledKey.getStatus()).isEqualTo(KeyStatus.ACTIVE);

        assertThat(securityService.getKeyByKeyId("key-android-001")).isPresent();
        assertThat(securityService.getActiveRevocationList()).isEmpty();
    }
}
