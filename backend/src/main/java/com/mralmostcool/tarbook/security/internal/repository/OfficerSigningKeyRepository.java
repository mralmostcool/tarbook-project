package com.mralmostcool.tarbook.security.internal.repository;

import com.mralmostcool.tarbook.security.internal.domain.KeyStatus;
import com.mralmostcool.tarbook.security.internal.domain.OfficerSigningKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfficerSigningKeyRepository extends JpaRepository<OfficerSigningKey, UUID> {
    Optional<OfficerSigningKey> findByKeyId(String keyId);
    List<OfficerSigningKey> findByOfficerUserId(UUID officerUserId);
    List<OfficerSigningKey> findByStatus(KeyStatus status);
}
