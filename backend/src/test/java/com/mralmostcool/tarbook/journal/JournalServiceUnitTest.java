package com.mralmostcool.tarbook.journal;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.SystemRole;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.journal.dto.CreateJournalEntryRequestDto;
import com.mralmostcool.tarbook.journal.dto.JournalEntryDto;
import com.mralmostcool.tarbook.journal.internal.domain.AuditLog;
import com.mralmostcool.tarbook.journal.internal.domain.JournalEntry;
import com.mralmostcool.tarbook.journal.internal.domain.JournalEntryStatus;
import com.mralmostcool.tarbook.journal.internal.repository.AuditLogRepository;
import com.mralmostcool.tarbook.journal.internal.repository.JournalEntryRepository;
import com.mralmostcool.tarbook.journal.internal.service.AuditLogInternalService;
import com.mralmostcool.tarbook.journal.internal.service.EvidenceStorageInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JournalServiceUnitTest {

    private JournalService journalService;
    private EvidenceStorageInternalService evidenceStorageInternalService;

    private List<JournalEntry> entryStore;
    private List<AuditLog> auditStore;

    private UUID cadetId;
    private UUID officerId;
    private AppUser cadetUser;
    private AppUser officerUser;

    @BeforeEach
    void setUp() {
        cadetId = UUID.randomUUID();
        officerId = UUID.randomUUID();
        entryStore = new ArrayList<>();
        auditStore = new ArrayList<>();

        cadetUser = AppUser.builder()
                .id(cadetId)
                .email("cadet.alex@maritime.org")
                .fullName("Cadet Alex")
                .systemRole(SystemRole.CANDIDATE)
                .build();

        officerUser = AppUser.builder()
                .id(officerId)
                .email("officer.smith@maritime.org")
                .fullName("Chief Officer Smith")
                .systemRole(SystemRole.OFFICER)
                .build();

        AppUserInternalService userInternalService = new AppUserInternalService(null) {
            @Override
            public Optional<AppUser> findById(UUID id) {
                if (cadetId.equals(id)) return Optional.of(cadetUser);
                if (officerId.equals(id)) return Optional.of(officerUser);
                return Optional.empty();
            }
        };

        JournalEntryRepository journalEntryRepository = new JournalEntryRepository() {
            @Override
            public JournalEntry save(JournalEntry entity) {
                entryStore.removeIf(e -> e.getId().equals(entity.getId()));
                entryStore.add(entity);
                return entity;
            }

            @Override
            public Optional<JournalEntry> findById(UUID id) {
                return entryStore.stream().filter(e -> e.getId().equals(id)).findFirst();
            }

            @Override
            public List<JournalEntry> findByCadetUserId(UUID userId) {
                return entryStore.stream().filter(e -> e.getCadetUser().getId().equals(userId)).toList();
            }

            @Override
            public List<JournalEntry> findByCadetUserIdAndStatus(UUID userId, JournalEntryStatus status) {
                return entryStore.stream().filter(e -> e.getCadetUser().getId().equals(userId) && e.getStatus() == status).toList();
            }

            @Override
            public <S extends JournalEntry> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override
            public boolean existsById(UUID uuid) { return false; }
            @Override
            public List<JournalEntry> findAll() { return entryStore; }
            @Override
            public List<JournalEntry> findAllById(Iterable<UUID> uuids) { return null; }
            @Override
            public long count() { return entryStore.size(); }
            @Override
            public void deleteById(UUID uuid) {}
            @Override
            public void delete(JournalEntry entity) {}
            @Override
            public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override
            public void deleteAll(Iterable<? extends JournalEntry> entities) {}
            @Override
            public void deleteAll() {}
            @Override
            public void flush() {}
            @Override
            public <S extends JournalEntry> S saveAndFlush(S entity) { return null; }
            @Override
            public <S extends JournalEntry> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override
            public void deleteAllInBatch(Iterable<JournalEntry> entities) {}
            @Override
            public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override
            public void deleteAllInBatch() {}
            @Override
            public JournalEntry getOne(UUID uuid) { return null; }
            @Override
            public JournalEntry getById(UUID uuid) { return null; }
            @Override
            public JournalEntry getReferenceById(UUID uuid) { return null; }
            @Override
            public <S extends JournalEntry> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override
            public <S extends JournalEntry> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override
            public <S extends JournalEntry> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override
            public <S extends JournalEntry> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override
            public <S extends JournalEntry> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override
            public <S extends JournalEntry> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override
            public <S extends JournalEntry, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override
            public List<JournalEntry> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override
            public org.springframework.data.domain.Page<JournalEntry> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        AuditLogRepository auditLogRepository = new AuditLogRepository() {
            @Override
            public AuditLog save(AuditLog entity) {
                auditStore.add(entity);
                return entity;
            }

            @Override
            public Optional<AuditLog> findTopByOrderByLoggedAtUtcDesc() {
                if (auditStore.isEmpty()) return Optional.empty();
                return Optional.of(auditStore.get(auditStore.size() - 1));
            }

            @Override
            public List<AuditLog> findByEntityNameAndEntityIdOrderByLoggedAtUtcAsc(String entityName, UUID entityId) {
                return auditStore.stream().filter(a -> a.getEntityName().equals(entityName) && a.getEntityId().equals(entityId)).toList();
            }

            @Override
            public <S extends AuditLog> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override
            public Optional<AuditLog> findById(UUID uuid) { return Optional.empty(); }
            @Override
            public boolean existsById(UUID uuid) { return false; }
            @Override
            public List<AuditLog> findAll() { return auditStore; }
            @Override
            public List<AuditLog> findAllById(Iterable<UUID> uuids) { return null; }
            @Override
            public long count() { return auditStore.size(); }
            @Override
            public void deleteById(UUID uuid) {}
            @Override
            public void delete(AuditLog entity) {}
            @Override
            public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override
            public void deleteAll(Iterable<? extends AuditLog> entities) {}
            @Override
            public void deleteAll() {}
            @Override
            public void flush() {}
            @Override
            public <S extends AuditLog> S saveAndFlush(S entity) { return null; }
            @Override
            public <S extends AuditLog> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override
            public void deleteAllInBatch(Iterable<AuditLog> entities) {}
            @Override
            public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override
            public void deleteAllInBatch() {}
            @Override
            public AuditLog getOne(UUID uuid) { return null; }
            @Override
            public AuditLog getById(UUID uuid) { return null; }
            @Override
            public AuditLog getReferenceById(UUID uuid) { return null; }
            @Override
            public <S extends AuditLog> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override
            public <S extends AuditLog> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override
            public <S extends AuditLog> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override
            public <S extends AuditLog> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override
            public <S extends AuditLog> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override
            public <S extends AuditLog> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override
            public <S extends AuditLog, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override
            public List<AuditLog> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override
            public org.springframework.data.domain.Page<AuditLog> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        AuditLogInternalService auditLogInternalService = new AuditLogInternalService(auditLogRepository);
        journalService = new JournalService(journalEntryRepository, userInternalService, auditLogInternalService);
        evidenceStorageInternalService = new EvidenceStorageInternalService(null);
    }

    @Test
    void shouldCreateJournalEntryInDraftStateAndGenerateAuditLog() {
        CreateJournalEntryRequestDto req = CreateJournalEntryRequestDto.builder()
                .cadetUserId(cadetId)
                .entryDate(LocalDate.now())
                .seaDaysLogged(new BigDecimal("1.0"))
                .watchkeepingHours(new BigDecimal("4.0"))
                .cadetComments("Navigational watchkeeping on bridge")
                .build();

        JournalEntryDto created = journalService.createJournalEntry(req);

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
        assertThat(created.getCadetUserId()).isEqualTo(cadetId);
        assertThat(auditStore).hasSize(1);
        assertThat(auditStore.get(0).getAction()).isEqualTo("CREATE_JOURNAL_ENTRY");
    }

    @Test
    void shouldSubmitJournalEntry() {
        CreateJournalEntryRequestDto req = CreateJournalEntryRequestDto.builder()
                .cadetUserId(cadetId)
                .entryDate(LocalDate.now())
                .seaDaysLogged(new BigDecimal("1.0"))
                .watchkeepingHours(new BigDecimal("4.0"))
                .build();
        JournalEntryDto created = journalService.createJournalEntry(req);

        JournalEntryDto submitted = journalService.submitJournalEntry(created.getId());

        assertThat(submitted.getStatus()).isEqualTo(JournalEntryStatus.SUBMITTED);
        assertThat(auditStore).hasSize(2);
        assertThat(auditStore.get(1).getAction()).isEqualTo("SUBMIT_JOURNAL_ENTRY");
    }

    @Test
    void shouldVerifyJournalEntryByOfficer() {
        CreateJournalEntryRequestDto req = CreateJournalEntryRequestDto.builder()
                .cadetUserId(cadetId)
                .entryDate(LocalDate.now())
                .seaDaysLogged(new BigDecimal("1.0"))
                .watchkeepingHours(new BigDecimal("4.0"))
                .build();
        JournalEntryDto created = journalService.createJournalEntry(req);

        JournalEntryDto verified = journalService.verifyJournalEntryByOfficer(created.getId(), officerId, "Good watchkeeping log");

        assertThat(verified.getStatus()).isEqualTo(JournalEntryStatus.VERIFIED);
        assertThat(verified.getSupervisorComments()).isEqualTo("Good watchkeeping log");
        assertThat(auditStore).hasSize(2);
        assertThat(auditStore.get(1).getAction()).isEqualTo("VERIFY_JOURNAL_ENTRY");
    }

    @Test
    void shouldComputeTamperEvidentSha256HashForEvidence() {
        byte[] payload = "EVIDENCE_PHOTO_BYTES_12345".getBytes();
        String hash = evidenceStorageInternalService.computeSha256Hash(payload);

        assertThat(hash).isNotBlank();
        assertThat(hash).hasSize(64);
    }
}
