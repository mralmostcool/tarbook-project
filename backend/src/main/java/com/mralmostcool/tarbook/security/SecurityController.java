package com.mralmostcool.tarbook.security;

import com.mralmostcool.tarbook.core.dto.ApiResponse;
import com.mralmostcool.tarbook.security.dto.AttestationChallengeDto;
import com.mralmostcool.tarbook.security.dto.KeyEnrollmentRequestDto;
import com.mralmostcool.tarbook.security.dto.SigningKeyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/officers/keys")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityService securityService;

    @PostMapping("/attestation-challenge")
    public ResponseEntity<ApiResponse<AttestationChallengeDto>> getAttestationChallenge() {
        AttestationChallengeDto challenge = securityService.generateAttestationChallenge();
        return ResponseEntity.ok(ApiResponse.success(challenge));
    }

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<SigningKeyDto>> enrollKey(@RequestBody KeyEnrollmentRequestDto request) {
        SigningKeyDto dto = securityService.enrollOfficerKey(request);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/{keyId}")
    public ResponseEntity<ApiResponse<SigningKeyDto>> getKeyByKeyId(@PathVariable String keyId) {
        return securityService.getKeyByKeyId(keyId)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/revocation-list")
    public ResponseEntity<ApiResponse<List<SigningKeyDto>>> getRevocationList() {
        List<SigningKeyDto> list = securityService.getActiveRevocationList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
