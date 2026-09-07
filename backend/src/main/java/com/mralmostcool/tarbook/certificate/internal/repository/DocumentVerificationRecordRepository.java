package com.mralmostcool.tarbook.certificate.internal.repository;

import com.mralmostcool.tarbook.certificate.internal.domain.DocumentVerificationRecord;
import com.mralmostcool.tarbook.certificate.internal.domain.TargetEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentVerificationRecordRepository extends JpaRepository<DocumentVerificationRecord, UUID> {

    List<DocumentVerificationRecord> findByTargetEntityTypeAndTargetEntityIdOrderByVerifiedAtUtcAsc(TargetEntityType targetEntityType, UUID targetEntityId);
}
