package com.mralmostcool.tarbook.certificate;

import com.mralmostcool.tarbook.certificate.dto.CreateSeafarerCertificateRequestDto;
import com.mralmostcool.tarbook.certificate.dto.CreateSeafarerDocumentRequestDto;
import com.mralmostcool.tarbook.certificate.dto.DocumentVerificationRecordDto;
import com.mralmostcool.tarbook.certificate.dto.SeafarerCertificateDto;
import com.mralmostcool.tarbook.certificate.dto.SeafarerDocumentDto;
import com.mralmostcool.tarbook.certificate.dto.VerifyDocumentRequestDto;
import com.mralmostcool.tarbook.certificate.internal.domain.TargetEntityType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/certificates-and-documents")
@RequiredArgsConstructor
@Tag(name = "Seafarer Documents & Certificates", description = "Endpoints for passport, CDC, INDOS, STCW modular safety certificates, and verification audit trails")
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping("/documents")
    @Operation(summary = "Create seafarer travel document (Passport, CDC, INDOS, SID)")
    public ResponseEntity<SeafarerDocumentDto> createDocument(@Valid @RequestBody CreateSeafarerDocumentRequestDto request) {
        return ResponseEntity.ok(certificateService.createSeafarerDocument(request));
    }

    @GetMapping("/documents/candidate/{candidateId}")
    @Operation(summary = "Get seafarer travel documents for candidate")
    public ResponseEntity<List<SeafarerDocumentDto>> getCandidateDocuments(@PathVariable UUID candidateId) {
        return ResponseEntity.ok(certificateService.getCandidateDocuments(candidateId));
    }

    @GetMapping("/documents/{id}")
    @Operation(summary = "Get seafarer document by ID")
    public ResponseEntity<SeafarerDocumentDto> getDocumentById(@PathVariable UUID id) {
        return certificateService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/certificates")
    @Operation(summary = "Create STCW modular safety certificate")
    public ResponseEntity<SeafarerCertificateDto> createCertificate(@Valid @RequestBody CreateSeafarerCertificateRequestDto request) {
        return ResponseEntity.ok(certificateService.createSeafarerCertificate(request));
    }

    @GetMapping("/certificates/candidate/{candidateId}")
    @Operation(summary = "Get STCW certificates for candidate")
    public ResponseEntity<List<SeafarerCertificateDto>> getCandidateCertificates(@PathVariable UUID candidateId) {
        return ResponseEntity.ok(certificateService.getCandidateCertificates(candidateId));
    }

    @GetMapping("/certificates/{id}")
    @Operation(summary = "Get STCW certificate by ID")
    public ResponseEntity<SeafarerCertificateDto> getCertificateById(@PathVariable UUID id) {
        return certificateService.getCertificateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/verifications")
    @Operation(summary = "Verify seafarer document or STCW certificate")
    public ResponseEntity<DocumentVerificationRecordDto> verifyDocument(@Valid @RequestBody VerifyDocumentRequestDto request) {
        return ResponseEntity.ok(certificateService.verifyDocumentOrCertificate(request));
    }

    @GetMapping("/verifications")
    @Operation(summary = "Get verification history for document or certificate")
    public ResponseEntity<List<DocumentVerificationRecordDto>> getVerifications(
            @RequestParam TargetEntityType targetEntityType,
            @RequestParam UUID targetEntityId) {
        return ResponseEntity.ok(certificateService.getVerifications(targetEntityType, targetEntityId));
    }
}
