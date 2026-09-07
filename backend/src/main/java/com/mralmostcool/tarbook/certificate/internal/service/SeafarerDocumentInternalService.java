package com.mralmostcool.tarbook.certificate.internal.service;

import com.mralmostcool.tarbook.certificate.dto.CreateSeafarerDocumentRequestDto;
import com.mralmostcool.tarbook.certificate.dto.SeafarerDocumentDto;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentStatus;
import com.mralmostcool.tarbook.certificate.internal.domain.SeafarerDocument;
import com.mralmostcool.tarbook.certificate.internal.repository.SeafarerDocumentRepository;
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
public class SeafarerDocumentInternalService {

    private final SeafarerDocumentRepository documentRepository;
    private final CandidateInternalService candidateInternalService;

    @Transactional
    public SeafarerDocumentDto createDocument(CreateSeafarerDocumentRequestDto request) {
        Candidate candidate = candidateInternalService.findById(request.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found: " + request.getCandidateId()));

        documentRepository.findByCandidateIdAndDocumentTypeAndDocumentNumber(request.getCandidateId(), request.getDocumentType(), request.getDocumentNumber())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Seafarer document already exists for this candidate");
                });

        OffsetDateTime now = OffsetDateTime.now();
        DocumentStatus status = (request.getExpiryDate() != null && request.getExpiryDate().isBefore(LocalDate.now()))
                ? DocumentStatus.EXPIRED : DocumentStatus.ACTIVE;

        SeafarerDocument document = SeafarerDocument.builder()
                .id(UUID.randomUUID())
                .candidate(candidate)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .issuingCountryCode(request.getIssuingCountryCode())
                .issuingAuthority(request.getIssuingAuthority())
                .placeOfIssue(request.getPlaceOfIssue())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .status(status)
                .createdAtUtc(now)
                .updatedAtUtc(now)
                .build();

        SeafarerDocument saved = documentRepository.save(document);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SeafarerDocumentDto> getCandidateDocuments(UUID candidateId) {
        return documentRepository.findByCandidateId(candidateId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SeafarerDocumentDto> getDocumentById(UUID id) {
        return documentRepository.findById(id).map(this::mapToDto);
    }

    public SeafarerDocumentDto mapToDto(SeafarerDocument doc) {
        return SeafarerDocumentDto.builder()
                .id(doc.getId())
                .candidateId(doc.getCandidate().getId())
                .documentType(doc.getDocumentType())
                .documentNumber(doc.getDocumentNumber())
                .issuingCountryCode(doc.getIssuingCountryCode())
                .issuingAuthority(doc.getIssuingAuthority())
                .placeOfIssue(doc.getPlaceOfIssue())
                .issueDate(doc.getIssueDate())
                .expiryDate(doc.getExpiryDate())
                .status(doc.getStatus())
                .createdAtUtc(doc.getCreatedAtUtc())
                .updatedAtUtc(doc.getUpdatedAtUtc())
                .build();
    }
}
