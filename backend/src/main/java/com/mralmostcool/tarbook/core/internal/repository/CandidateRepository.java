package com.mralmostcool.tarbook.core.internal.repository;

import com.mralmostcool.tarbook.core.internal.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
    Optional<Candidate> findByIndosNumber(String indosNumber);
    Optional<Candidate> findByCdcNumber(String cdcNumber);
}
