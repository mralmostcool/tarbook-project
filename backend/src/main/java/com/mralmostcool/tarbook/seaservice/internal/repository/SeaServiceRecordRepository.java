package com.mralmostcool.tarbook.seaservice.internal.repository;

import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceRecord;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeaServiceRecordRepository extends JpaRepository<SeaServiceRecord, UUID> {

    List<SeaServiceRecord> findByCandidateId(UUID candidateId);

    List<SeaServiceRecord> findByTarBookId(UUID tarBookId);

    List<SeaServiceRecord> findByCandidateIdAndStatus(UUID candidateId, SeaServiceStatus status);
}
