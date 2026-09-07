package com.mralmostcool.tarbook.certificate;

import com.mralmostcool.tarbook.certificate.dto.CreateSeafarerCertificateRequestDto;
import com.mralmostcool.tarbook.certificate.dto.CreateSeafarerDocumentRequestDto;
import com.mralmostcool.tarbook.certificate.dto.DocumentVerificationRecordDto;
import com.mralmostcool.tarbook.certificate.dto.SeafarerCertificateDto;
import com.mralmostcool.tarbook.certificate.dto.SeafarerDocumentDto;
import com.mralmostcool.tarbook.certificate.dto.VerifyDocumentRequestDto;
import com.mralmostcool.tarbook.certificate.internal.domain.CertificateType;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentStatus;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentType;
import com.mralmostcool.tarbook.certificate.internal.domain.DocumentVerificationRecord;
import com.mralmostcool.tarbook.certificate.internal.domain.SeafarerCertificate;
import com.mralmostcool.tarbook.certificate.internal.domain.SeafarerDocument;
import com.mralmostcool.tarbook.certificate.internal.domain.TargetEntityType;
import com.mralmostcool.tarbook.certificate.internal.domain.VerificationDecision;
import com.mralmostcool.tarbook.certificate.internal.repository.DocumentVerificationRecordRepository;
import com.mralmostcool.tarbook.certificate.internal.repository.SeafarerCertificateRepository;
import com.mralmostcool.tarbook.certificate.internal.repository.SeafarerDocumentRepository;
import com.mralmostcool.tarbook.certificate.internal.service.DocumentVerificationInternalService;
import com.mralmostcool.tarbook.certificate.internal.service.SeafarerCertificateInternalService;
import com.mralmostcool.tarbook.certificate.internal.service.SeafarerDocumentInternalService;
import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.Candidate;
import com.mralmostcool.tarbook.core.internal.domain.Organization;
import com.mralmostcool.tarbook.core.internal.domain.OrganizationType;
import com.mralmostcool.tarbook.core.internal.domain.SystemRole;
import com.mralmostcool.tarbook.core.internal.domain.TrainingStream;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.core.internal.service.CandidateInternalService;
import com.mralmostcool.tarbook.core.internal.service.OrganizationInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CertificateServiceUnitTest {

    private CertificateService certificateService;

    private List<SeafarerDocument> documentStore;
    private List<SeafarerCertificate> certificateStore;
    private List<DocumentVerificationRecord> verificationStore;

    private UUID candidateId;
    private UUID verifierUserId;
    private UUID verifierOrgId;

    private Candidate candidate;
    private AppUser verifierUser;
    private Organization verifierOrg;

    @BeforeEach
    void setUp() {
        candidateId = UUID.randomUUID();
        verifierUserId = UUID.randomUUID();
        verifierOrgId = UUID.randomUUID();

        documentStore = new ArrayList<>();
        certificateStore = new ArrayList<>();
        verificationStore = new ArrayList<>();

        candidate = Candidate.builder()
                .id(candidateId)
                .indosNumber("26N1234")
                .cdcNumber("C12345")
                .trainingStream(TrainingStream.DECK_CADET)
                .dateOfBirth(LocalDate.of(2000, 5, 15))
                .build();

        verifierUser = AppUser.builder()
                .id(verifierUserId)
                .email("verifier@mti.edu")
                .fullName("Dr. MTI Verifier")
                .systemRole(SystemRole.ADMIN)
                .build();

        verifierOrg = Organization.builder()
                .id(verifierOrgId)
                .name("Anglo-Eastern Maritime Training Institute")
                .code("AEMTI-001")
                .type(OrganizationType.MTI)
                .build();

        CandidateInternalService candidateInternalService = new CandidateInternalService(null) {
            @Override
            public Optional<Candidate> findById(UUID id) {
                if (candidateId.equals(id)) return Optional.of(candidate);
                return Optional.empty();
            }
        };

        AppUserInternalService userInternalService = new AppUserInternalService(null) {
            @Override
            public Optional<AppUser> findById(UUID id) {
                if (verifierUserId.equals(id)) return Optional.of(verifierUser);
                return Optional.empty();
            }
        };

        OrganizationInternalService orgInternalService = new OrganizationInternalService(null) {
            @Override
            public Optional<Organization> findById(UUID id) {
                if (verifierOrgId.equals(id)) return Optional.of(verifierOrg);
                return Optional.empty();
            }
        };

        SeafarerDocumentRepository documentRepository = new SeafarerDocumentRepository() {
            @Override
            public SeafarerDocument save(SeafarerDocument entity) {
                documentStore.removeIf(d -> d.getId().equals(entity.getId()));
                documentStore.add(entity);
                return entity;
            }

            @Override
            public Optional<SeafarerDocument> findById(UUID id) {
                return documentStore.stream().filter(d -> d.getId().equals(id)).findFirst();
            }

            @Override
            public List<SeafarerDocument> findByCandidateId(UUID cId) {
                return documentStore.stream().filter(d -> d.getCandidate().getId().equals(cId)).toList();
            }

            @Override
            public List<SeafarerDocument> findByCandidateIdAndStatus(UUID cId, DocumentStatus status) {
                return documentStore.stream().filter(d -> d.getCandidate().getId().equals(cId) && d.getStatus() == status).toList();
            }

            @Override
            public Optional<SeafarerDocument> findByCandidateIdAndDocumentTypeAndDocumentNumber(UUID cId, DocumentType dt, String dn) {
                return documentStore.stream().filter(d -> d.getCandidate().getId().equals(cId) && d.getDocumentType() == dt && d.getDocumentNumber().equals(dn)).findFirst();
            }

            @Override public <S extends SeafarerDocument> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public List<SeafarerDocument> findAll() { return documentStore; }
            @Override public List<SeafarerDocument> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return documentStore.size(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(SeafarerDocument entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends SeafarerDocument> entities) {}
            @Override public void deleteAll() {}
            @Override public void flush() {}
            @Override public <S extends SeafarerDocument> S saveAndFlush(S entity) { return null; }
            @Override public <S extends SeafarerDocument> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<SeafarerDocument> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public SeafarerDocument getOne(UUID uuid) { return null; }
            @Override public SeafarerDocument getById(UUID uuid) { return null; }
            @Override public SeafarerDocument getReferenceById(UUID uuid) { return null; }
            @Override public <S extends SeafarerDocument> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends SeafarerDocument> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends SeafarerDocument> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends SeafarerDocument> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends SeafarerDocument> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends SeafarerDocument> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends SeafarerDocument, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public List<SeafarerDocument> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<SeafarerDocument> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        SeafarerCertificateRepository certificateRepository = new SeafarerCertificateRepository() {
            @Override
            public SeafarerCertificate save(SeafarerCertificate entity) {
                certificateStore.removeIf(c -> c.getId().equals(entity.getId()));
                certificateStore.add(entity);
                return entity;
            }

            @Override
            public Optional<SeafarerCertificate> findById(UUID id) {
                return certificateStore.stream().filter(c -> c.getId().equals(id)).findFirst();
            }

            @Override
            public List<SeafarerCertificate> findByCandidateId(UUID cId) {
                return certificateStore.stream().filter(c -> c.getCandidate().getId().equals(cId)).toList();
            }

            @Override
            public List<SeafarerCertificate> findByCandidateIdAndStatus(UUID cId, DocumentStatus status) {
                return certificateStore.stream().filter(c -> c.getCandidate().getId().equals(cId) && c.getStatus() == status).toList();
            }

            @Override
            public List<SeafarerCertificate> findByCandidateIdAndCertificateType(UUID cId, CertificateType ct) {
                return certificateStore.stream().filter(c -> c.getCandidate().getId().equals(cId) && c.getCertificateType() == ct).toList();
            }

            @Override public <S extends SeafarerCertificate> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public List<SeafarerCertificate> findAll() { return certificateStore; }
            @Override public List<SeafarerCertificate> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return certificateStore.size(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(SeafarerCertificate entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends SeafarerCertificate> entities) {}
            @Override public void deleteAll() {}
            @Override public void flush() {}
            @Override public <S extends SeafarerCertificate> S saveAndFlush(S entity) { return null; }
            @Override public <S extends SeafarerCertificate> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<SeafarerCertificate> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public SeafarerCertificate getOne(UUID uuid) { return null; }
            @Override public SeafarerCertificate getById(UUID uuid) { return null; }
            @Override public SeafarerCertificate getReferenceById(UUID uuid) { return null; }
            @Override public <S extends SeafarerCertificate> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends SeafarerCertificate> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends SeafarerCertificate> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends SeafarerCertificate> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends SeafarerCertificate> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends SeafarerCertificate> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends SeafarerCertificate, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public List<SeafarerCertificate> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<SeafarerCertificate> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        DocumentVerificationRecordRepository verificationRepository = new DocumentVerificationRecordRepository() {
            @Override
            public DocumentVerificationRecord save(DocumentVerificationRecord entity) {
                verificationStore.add(entity);
                return entity;
            }

            @Override
            public List<DocumentVerificationRecord> findByTargetEntityTypeAndTargetEntityIdOrderByVerifiedAtUtcAsc(TargetEntityType type, UUID targetId) {
                return verificationStore.stream().filter(v -> v.getTargetEntityType() == type && v.getTargetEntityId().equals(targetId)).toList();
            }

            @Override public <S extends DocumentVerificationRecord> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public Optional<DocumentVerificationRecord> findById(UUID uuid) { return Optional.empty(); }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public List<DocumentVerificationRecord> findAll() { return verificationStore; }
            @Override public List<DocumentVerificationRecord> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return verificationStore.size(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(DocumentVerificationRecord entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends DocumentVerificationRecord> entities) {}
            @Override public void deleteAll() {}
            @Override public void flush() {}
            @Override public <S extends DocumentVerificationRecord> S saveAndFlush(S entity) { return null; }
            @Override public <S extends DocumentVerificationRecord> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<DocumentVerificationRecord> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public DocumentVerificationRecord getOne(UUID uuid) { return null; }
            @Override public DocumentVerificationRecord getById(UUID uuid) { return null; }
            @Override public DocumentVerificationRecord getReferenceById(UUID uuid) { return null; }
            @Override public <S extends DocumentVerificationRecord> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends DocumentVerificationRecord> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends DocumentVerificationRecord> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends DocumentVerificationRecord> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends DocumentVerificationRecord> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends DocumentVerificationRecord> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends DocumentVerificationRecord, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public List<DocumentVerificationRecord> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<DocumentVerificationRecord> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        SeafarerDocumentInternalService documentService = new SeafarerDocumentInternalService(documentRepository, candidateInternalService);
        SeafarerCertificateInternalService certificateInternalService = new SeafarerCertificateInternalService(certificateRepository, candidateInternalService);
        DocumentVerificationInternalService verificationService = new DocumentVerificationInternalService(verificationRepository, userInternalService, orgInternalService);

        certificateService = new CertificateService(documentService, certificateInternalService, verificationService);
    }

    @Test
    void shouldCreateSeafarerDocumentInActiveState() {
        CreateSeafarerDocumentRequestDto req = CreateSeafarerDocumentRequestDto.builder()
                .candidateId(candidateId)
                .documentType(DocumentType.PASSPORT)
                .documentNumber("Z9876543")
                .issuingCountryCode("IN")
                .issuingAuthority("Passport Office Mumbai")
                .placeOfIssue("Mumbai")
                .issueDate(LocalDate.of(2022, 1, 1))
                .expiryDate(LocalDate.of(2032, 1, 1))
                .build();

        SeafarerDocumentDto created = certificateService.createSeafarerDocument(req);

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(DocumentStatus.ACTIVE);
        assertThat(created.getDocumentNumber()).isEqualTo("Z9876543");
        assertThat(created.getDocumentType()).isEqualTo(DocumentType.PASSPORT);
    }

    @Test
    void shouldCreateSeafarerCertificate() {
        CreateSeafarerCertificateRequestDto req = CreateSeafarerCertificateRequestDto.builder()
                .candidateId(candidateId)
                .certificateType(CertificateType.PST)
                .certificateNumber("PST-2026-99")
                .issuingMtiName("AEMTI")
                .issuingMtiCode("AEMTI-001")
                .issueDate(LocalDate.of(2025, 6, 1))
                .expiryDate(LocalDate.of(2030, 6, 1))
                .build();

        SeafarerCertificateDto created = certificateService.createSeafarerCertificate(req);

        assertThat(created).isNotNull();
        assertThat(created.getCertificateType()).isEqualTo(CertificateType.PST);
        assertThat(created.getStatus()).isEqualTo(DocumentStatus.ACTIVE);
    }

    @Test
    void shouldVerifyDocumentAndGenerateJcsPayloadAndDigest() {
        CreateSeafarerDocumentRequestDto docReq = CreateSeafarerDocumentRequestDto.builder()
                .candidateId(candidateId)
                .documentType(DocumentType.CDC)
                .documentNumber("C998877")
                .issuingCountryCode("IN")
                .issueDate(LocalDate.of(2023, 1, 1))
                .build();
        SeafarerDocumentDto doc = certificateService.createSeafarerDocument(docReq);

        VerifyDocumentRequestDto verifyReq = VerifyDocumentRequestDto.builder()
                .targetEntityType(TargetEntityType.SEAFARER_DOCUMENT)
                .targetEntityId(doc.getId())
                .verifierUserId(verifierUserId)
                .verifierOrgId(verifierOrgId)
                .decision(VerificationDecision.APPROVED)
                .decisionReason("Original CDC inspected and verified")
                .rawEvidencePayload("{\"cdc_stamp\":\"VERIFIED\"}")
                .build();

        DocumentVerificationRecordDto verified = certificateService.verifyDocumentOrCertificate(verifyReq);

        assertThat(verified).isNotNull();
        assertThat(verified.getDecision()).isEqualTo(VerificationDecision.APPROVED);
        assertThat(verified.getEvidenceDigestSha256()).hasSize(64);
        assertThat(verified.getCanonicalPayloadJcs()).contains("APPROVED");
    }

    @Test
    void shouldPreventDuplicateSeafarerDocument() {
        CreateSeafarerDocumentRequestDto req = CreateSeafarerDocumentRequestDto.builder()
                .candidateId(candidateId)
                .documentType(DocumentType.INDOS)
                .documentNumber("INDOS-12345")
                .issuingCountryCode("IN")
                .issueDate(LocalDate.of(2021, 1, 1))
                .build();

        certificateService.createSeafarerDocument(req);

        assertThatThrownBy(() -> certificateService.createSeafarerDocument(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }
}
