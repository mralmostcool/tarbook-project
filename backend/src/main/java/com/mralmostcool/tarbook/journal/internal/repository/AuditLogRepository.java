package com.mralmostcool.tarbook.journal.internal.repository;

import com.mralmostcool.tarbook.journal.internal.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByEntityNameAndEntityIdOrderByLoggedAtUtcAsc(String entityName, UUID entityId);
    Optional<AuditLog> findTopByOrderByLoggedAtUtcDesc();
}
