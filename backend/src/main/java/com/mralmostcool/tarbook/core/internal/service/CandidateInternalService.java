package com.mralmostcool.tarbook.core.internal.service;

import com.mralmostcool.tarbook.core.internal.domain.Candidate;
import com.mralmostcool.tarbook.core.internal.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateInternalService {

    private final CandidateRepository candidateRepository;

    @Transactional(readOnly = true)
    public Optional<Candidate> findById(UUID id) {
        return candidateRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Candidate> findByIndosNumber(String indosNumber) {
        return candidateRepository.findByIndosNumber(indosNumber);
    }

    @Transactional
    public Candidate save(Candidate candidate) {
        OffsetDateTime now = OffsetDateTime.now();
        if (candidate.getCreatedAtUtc() == null) {
            candidate.setCreatedAtUtc(now);
        }
        candidate.setUpdatedAtUtc(now);
        return candidateRepository.save(candidate);
    }
}
