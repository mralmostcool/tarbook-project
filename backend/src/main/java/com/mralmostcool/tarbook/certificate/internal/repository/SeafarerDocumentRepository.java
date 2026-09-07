package com.mralmostcool.tarbook.certificate.internal.repository;

import com.mralmostcool.tarbook.certificate.internal.domain.DocumentStatus;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentType;
import com.mralmostcool.tarbook.certificate.internal.domain.SeafarerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeafarerDocumentRepository extends JpaRepository<SeafarerDocument, UUID> {

    List<SeafarerDocument> findByCandidateId(UUID candidateId);

    List<SeafarerDocument> findByCandidateIdAndStatus(UUID candidateId, DocumentStatus status);

    Optional<SeafarerDocument> findByCandidateIdAndDocumentTypeAndDocumentNumber(UUID candidateId, DocumentType documentType, String documentNumber);
}
