package com.mralmostcool.tarbook.certificate.internal.repository;

import com.mralmostcool.tarbook.certificate.internal.domain.CertificateType;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentStatus;
import com.mralmostcool.tarbook.certificate.internal.domain.SeafarerCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeafarerCertificateRepository extends JpaRepository<SeafarerCertificate, UUID> {

    List<SeafarerCertificate> findByCandidateId(UUID candidateId);

    List<SeafarerCertificate> findByCandidateIdAndStatus(UUID candidateId, DocumentStatus status);

    List<SeafarerCertificate> findByCandidateIdAndCertificateType(UUID candidateId, CertificateType certificateType);
}
