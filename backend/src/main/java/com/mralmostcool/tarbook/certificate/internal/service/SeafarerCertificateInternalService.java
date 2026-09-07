package com.mralmostcool.tarbook.certificate.internal.service;

import com.mralmostcool.tarbook.certificate.dto.CreateSeafarerCertificateRequestDto;
import com.mralmostcool.tarbook.certificate.dto.SeafarerCertificateDto;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentStatus;
import com.mralmostcool.tarbook.certificate.internal.domain.SeafarerCertificate;
import com.mralmostcool.tarbook.certificate.internal.repository.SeafarerCertificateRepository;
import com.mralmostcool.tarbook.core.internal.domain.Candidate;
import com.mralmostcool.tarbook.core.internal.service.CandidateInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeafarerCertificateInternalService {

    private final SeafarerCertificateRepository certificateRepository;
    private final CandidateInternalService candidateInternalService;

    @Transactional
    public SeafarerCertificateDto createCertificate(CreateSeafarerCertificateRequestDto request) {
        Candidate candidate = candidateInternalService.findById(request.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found: " + request.getCandidateId()));

        OffsetDateTime now = OffsetDateTime.now();
        DocumentStatus status = (request.getExpiryDate() != null && request.getExpiryDate().isBefore(LocalDate.now()))
                ? DocumentStatus.EXPIRED : DocumentStatus.ACTIVE;

        SeafarerCertificate certificate = SeafarerCertificate.builder()
                .id(UUID.randomUUID())
                .candidate(candidate)
                .certificateType(request.getCertificateType())
                .certificateNumber(request.getCertificateNumber())
                .issuingMtiName(request.getIssuingMtiName())
                .issuingMtiCode(request.getIssuingMtiCode())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .status(status)
                .createdAtUtc(now)
                .updatedAtUtc(now)
                .build();

        SeafarerCertificate saved = certificateRepository.save(certificate);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SeafarerCertificateDto> getCandidateCertificates(UUID candidateId) {
        return certificateRepository.findByCandidateId(candidateId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SeafarerCertificateDto> getCertificateById(UUID id) {
        return certificateRepository.findById(id).map(this::mapToDto);
    }

    public SeafarerCertificateDto mapToDto(SeafarerCertificate cert) {
        return SeafarerCertificateDto.builder()
                .id(cert.getId())
                .candidateId(cert.getCandidate().getId())
                .certificateType(cert.getCertificateType())
                .certificateNumber(cert.getCertificateNumber())
                .issuingMtiName(cert.getIssuingMtiName())
                .issuingMtiCode(cert.getIssuingMtiCode())
                .issueDate(cert.getIssueDate())
                .expiryDate(cert.getExpiryDate())
                .status(cert.getStatus())
                .replacedByCertificateId(cert.getReplacedByCertificateId())
                .createdAtUtc(cert.getCreatedAtUtc())
                .updatedAtUtc(cert.getUpdatedAtUtc())
                .build();
    }
}
