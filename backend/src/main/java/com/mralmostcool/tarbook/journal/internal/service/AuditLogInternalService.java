package com.mralmostcool.tarbook.journal.internal.service;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.journal.internal.domain.AuditLog;
import com.mralmostcool.tarbook.journal.internal.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogInternalService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog logAction(AppUser actorUser, String action, String entityName, UUID entityId, String detailsJson) {
        Optional<AuditLog> latestLogOpt = auditLogRepository.findTopByOrderByLoggedAtUtcDesc();
        String prevHash = latestLogOpt.map(AuditLog::getEntryHash).orElse("GENESIS_HASH_000000000000000000000000000000000000000000000000");

        OffsetDateTime now = OffsetDateTime.now();
        String payload = String.format("%s|%s|%s|%s|%s|%s|%s",
                prevHash,
                actorUser.getId(),
                action,
                entityName,
                entityId,
                detailsJson != null ? detailsJson : "",
                now);

        String entryHash = computeHash(payload);

        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .actorUser(actorUser)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .detailsJson(detailsJson)
                .prevHash(prevHash)
                .entryHash(entryHash)
                .loggedAtUtc(now)
                .build();

        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findLogsByEntity(String entityName, UUID entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByLoggedAtUtcAsc(entityName, entityId);
    }

    private String computeHash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
