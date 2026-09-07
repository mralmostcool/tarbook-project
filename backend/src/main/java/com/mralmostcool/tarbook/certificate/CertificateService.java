package com.mralmostcool.tarbook.certificate;

import com.mralmostcool.tarbook.certificate.dto.CreateSeafarerCertificateRequestDto;
import com.mralmostcool.tarbook.certificate.dto.CreateSeafarerDocumentRequestDto;
import com.mralmostcool.tarbook.certificate.dto.DocumentVerificationRecordDto;
import com.mralmostcool.tarbook.certificate.dto.SeafarerCertificateDto;
import com.mralmostcool.tarbook.certificate.dto.SeafarerDocumentDto;
import com.mralmostcool.tarbook.certificate.dto.VerifyDocumentRequestDto;
import com.mralmostcool.tarbook.certificate.internal.domain.TargetEntityType;
import com.mralmostcool.tarbook.certificate.internal.service.DocumentVerificationInternalService;
import com.mralmostcool.tarbook.certificate.internal.service.SeafarerCertificateInternalService;
import com.mralmostcool.tarbook.certificate.internal.service.SeafarerDocumentInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final SeafarerDocumentInternalService documentInternalService;
    private final SeafarerCertificateInternalService certificateInternalService;
    private final DocumentVerificationInternalService verificationInternalService;

    @Transactional
    public SeafarerDocumentDto createSeafarerDocument(CreateSeafarerDocumentRequestDto request) {
        return documentInternalService.createDocument(request);
    }

    @Transactional(readOnly = true)
    public List<SeafarerDocumentDto> getCandidateDocuments(UUID candidateId) {
        return documentInternalService.getCandidateDocuments(candidateId);
    }

    @Transactional(readOnly = true)
    public Optional<SeafarerDocumentDto> getDocumentById(UUID id) {
        return documentInternalService.getDocumentById(id);
    }

    @Transactional
    public SeafarerCertificateDto createSeafarerCertificate(CreateSeafarerCertificateRequestDto request) {
        return certificateInternalService.createCertificate(request);
    }

    @Transactional(readOnly = true)
    public List<SeafarerCertificateDto> getCandidateCertificates(UUID candidateId) {
        return certificateInternalService.getCandidateCertificates(candidateId);
    }

    @Transactional(readOnly = true)
    public Optional<SeafarerCertificateDto> getCertificateById(UUID id) {
        return certificateInternalService.getCertificateById(id);
    }

    @Transactional
    public DocumentVerificationRecordDto verifyDocumentOrCertificate(VerifyDocumentRequestDto request) {
        return verificationInternalService.verifyDocument(request);
    }

    @Transactional(readOnly = true)
    public List<DocumentVerificationRecordDto> getVerifications(TargetEntityType targetEntityType, UUID targetEntityId) {
        return verificationInternalService.getVerificationsForTarget(targetEntityType, targetEntityId);
    }
}
