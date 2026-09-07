package com.mralmostcool.tarbook.seaservice.internal.service;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.seaservice.dto.EndorseSeaServiceRecordRequestDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceEndorsementDto;
import com.mralmostcool.tarbook.seaservice.internal.domain.EndorsementType;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceEndorsement;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceRecord;
import com.mralmostcool.tarbook.seaservice.internal.repository.SeaServiceEndorsementRepository;
import com.mralmostcool.tarbook.seaservice.internal.repository.SeaServiceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MasterEndorsementInternalService {

    private final SeaServiceRecordRepository recordRepository;
    private final SeaServiceEndorsementRepository endorsementRepository;
    private final AppUserInternalService userInternalService;

    @Transactional
    public SeaServiceEndorsementDto endorseSeaService(UUID recordId, EndorseSeaServiceRecordRequestDto request) {
        SeaServiceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Sea service record not found: " + recordId));

        AppUser endorser = userInternalService.findById(request.getEndorserUserId())
                .orElseThrow(() -> new IllegalArgumentException("Endorser user not found: " + request.getEndorserUserId()));

        if (request.getEndorsementType() == EndorsementType.FINAL_DISCHARGE) {
            endorsementRepository.findBySeaServiceRecordIdAndEndorsementType(recordId, EndorsementType.FINAL_DISCHARGE)
                    .ifPresent(existing -> {
                        throw new IllegalStateException("Sea service record already has a FINAL_DISCHARGE endorsement");
                    });
        }

        OffsetDateTime now = OffsetDateTime.now();
        UUID nonce = UUID.randomUUID();
        String payloadToHash = String.format("%s:%s:%s:%s:%s:%s",
                recordId, request.getEndorserUserId(), request.getEndorserRole(),
                request.getEndorsementType(), request.getKeyId(), nonce);
        String payloadHash = computeSha256(payloadToHash);

        byte[] sigBytes = request.getSignatureBytes() != null ? request.getSignatureBytes() : new byte[0];

        SeaServiceEndorsement endorsement = SeaServiceEndorsement.builder()
                .id(UUID.randomUUID())
                .seaServiceRecord(record)
                .endorserUser(endorser)
                .endorserRole(request.getEndorserRole())
                .endorsementType(request.getEndorsementType())
                .conductRating(request.getConductRating())
                .abilityRating(request.getAbilityRating())
                .comments(request.getComments())
                .keyId(request.getKeyId())
                .signingNonce(nonce)
                .signaturePayloadHash(payloadHash)
                .signatureBytes(sigBytes)
                .signedAtUtc(now)
                .createdAtUtc(now)
                .build();

        SeaServiceEndorsement saved = endorsementRepository.save(endorsement);

        return SeaServiceEndorsementDto.builder()
                .id(saved.getId())
                .seaServiceId(record.getId())
                .endorserUserId(endorser.getId())
                .endorserName(endorser.getFullName())
                .endorserRole(saved.getEndorserRole())
                .endorsementType(saved.getEndorsementType())
                .conductRating(saved.getConductRating())
                .abilityRating(saved.getAbilityRating())
                .comments(saved.getComments())
                .keyId(saved.getKeyId())
                .signaturePayloadHash(saved.getSignaturePayloadHash())
                .signedAtUtc(saved.getSignedAtUtc())
                .build();
    }

    private String computeSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }
}
