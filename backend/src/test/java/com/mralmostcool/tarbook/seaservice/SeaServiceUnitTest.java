package com.mralmostcool.tarbook.seaservice;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.SystemRole;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.seaservice.dto.CreateSeaServiceRecordRequestDto;
import com.mralmostcool.tarbook.seaservice.dto.EndorseSeaServiceRecordRequestDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceEndorsementDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceRecordDto;
import com.mralmostcool.tarbook.seaservice.internal.domain.EndorserRole;
import com.mralmostcool.tarbook.seaservice.internal.domain.EndorsementType;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceEndorsement;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceRecord;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceStatus;
import com.mralmostcool.tarbook.seaservice.internal.repository.SeaServiceEndorsementRepository;
import com.mralmostcool.tarbook.seaservice.internal.repository.SeaServiceRecordRepository;
import com.mralmostcool.tarbook.seaservice.internal.service.MasterEndorsementInternalService;
import com.mralmostcool.tarbook.seaservice.internal.service.SeaServiceRecordInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeaServiceUnitTest {

    private SeaService seaService;

    private List<SeaServiceRecord> recordStore;
    private List<SeaServiceEndorsement> endorsementStore;

    private UUID tarBookId;
    private UUID candidateId;
    private UUID masterId;
    private AppUser masterUser;

    @BeforeEach
    void setUp() {
        tarBookId = UUID.randomUUID();
        candidateId = UUID.randomUUID();
        masterId = UUID.randomUUID();

        recordStore = new ArrayList<>();
        endorsementStore = new ArrayList<>();

        masterUser = AppUser.builder()
                .id(masterId)
                .email("master.vessel@maritime.org")
                .fullName("Captain James Cook")
                .systemRole(SystemRole.MASTER)
                .build();

        AppUserInternalService userInternalService = new AppUserInternalService(null) {
            @Override
            public Optional<AppUser> findById(UUID id) {
                if (masterId.equals(id)) return Optional.of(masterUser);
                return Optional.empty();
            }
        };

        SeaServiceRecordRepository recordRepository = new SeaServiceRecordRepository() {
            @Override
            public <S extends SeaServiceRecord> S save(S entity) {
                recordStore.removeIf(r -> r.getId().equals(entity.getId()));
                recordStore.add(entity);
                return entity;
            }

            @Override
            public Optional<SeaServiceRecord> findById(UUID id) {
                return recordStore.stream().filter(r -> r.getId().equals(id)).findFirst();
            }

            @Override
            public List<SeaServiceRecord> findByCandidateId(UUID cId) {
                return recordStore.stream().filter(r -> r.getCandidateId().equals(cId)).toList();
            }

            @Override
            public List<SeaServiceRecord> findByTarBookId(UUID tbId) {
                return recordStore.stream().filter(r -> r.getTarBookId().equals(tbId)).toList();
            }

            @Override
            public List<SeaServiceRecord> findByCandidateIdAndStatus(UUID cId, SeaServiceStatus status) {
                return recordStore.stream().filter(r -> r.getCandidateId().equals(cId) && r.getStatus() == status).toList();
            }

            @Override public <S extends SeaServiceRecord> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public List<SeaServiceRecord> findAll() { return recordStore; }
            @Override public List<SeaServiceRecord> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return recordStore.size(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(SeaServiceRecord entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends SeaServiceRecord> entities) {}
            @Override public void deleteAll() {}
            @Override public void flush() {}
            @Override public <S extends SeaServiceRecord> S saveAndFlush(S entity) { return null; }
            @Override public <S extends SeaServiceRecord> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<SeaServiceRecord> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public SeaServiceRecord getOne(UUID uuid) { return null; }
            @Override public SeaServiceRecord getById(UUID uuid) { return null; }
            @Override public SeaServiceRecord getReferenceById(UUID uuid) { return null; }
            @Override public <S extends SeaServiceRecord> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends SeaServiceRecord> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends SeaServiceRecord> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends SeaServiceRecord> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends SeaServiceRecord> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends SeaServiceRecord> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends SeaServiceRecord, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public List<SeaServiceRecord> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<SeaServiceRecord> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        SeaServiceEndorsementRepository endorsementRepository = new SeaServiceEndorsementRepository() {
            @Override
            public <S extends SeaServiceEndorsement> S save(S entity) {
                endorsementStore.add(entity);
                return entity;
            }

            @Override
            public List<SeaServiceEndorsement> findBySeaServiceRecordId(UUID ssId) {
                return endorsementStore.stream().filter(e -> e.getSeaServiceRecord().getId().equals(ssId)).toList();
            }

            @Override
            public Optional<SeaServiceEndorsement> findBySeaServiceRecordIdAndEndorsementType(UUID ssId, EndorsementType type) {
                return endorsementStore.stream().filter(e -> e.getSeaServiceRecord().getId().equals(ssId) && e.getEndorsementType() == type).findFirst();
            }

            @Override public <S extends SeaServiceEndorsement> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public Optional<SeaServiceEndorsement> findById(UUID uuid) { return Optional.empty(); }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public List<SeaServiceEndorsement> findAll() { return endorsementStore; }
            @Override public List<SeaServiceEndorsement> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return endorsementStore.size(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(SeaServiceEndorsement entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends SeaServiceEndorsement> entities) {}
            @Override public void deleteAll() {}
            @Override public void flush() {}
            @Override public <S extends SeaServiceEndorsement> S saveAndFlush(S entity) { return null; }
            @Override public <S extends SeaServiceEndorsement> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<SeaServiceEndorsement> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public SeaServiceEndorsement getOne(UUID uuid) { return null; }
            @Override public SeaServiceEndorsement getById(UUID uuid) { return null; }
            @Override public SeaServiceEndorsement getReferenceById(UUID uuid) { return null; }
            @Override public <S extends SeaServiceEndorsement> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends SeaServiceEndorsement> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends SeaServiceEndorsement> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends SeaServiceEndorsement> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends SeaServiceEndorsement> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends SeaServiceEndorsement> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends SeaServiceEndorsement, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public List<SeaServiceEndorsement> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<SeaServiceEndorsement> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        SeaServiceRecordInternalService recordService = new SeaServiceRecordInternalService(recordRepository, endorsementRepository);
        MasterEndorsementInternalService endorsementService = new MasterEndorsementInternalService(recordRepository, endorsementRepository, userInternalService);

        seaService = new SeaService(recordService, endorsementService);
    }

    @Test
    void shouldCreateSeaServiceRecordInInProgressState() {
        CreateSeaServiceRecordRequestDto req = CreateSeaServiceRecordRequestDto.builder()
                .tarBookId(tarBookId)
                .candidateId(candidateId)
                .vesselName("MV PACIFIC EXPLORER")
                .vesselImo("9876543")
                .flagState("PANAMA")
                .vesselType("CONTAINER")
                .grossTonnage(new BigDecimal("45000.00"))
                .signOnDate(LocalDate.now().minusMonths(3))
                .daysAtSea(60)
                .daysInPort(30)
                .bridgeWatchHoursDay(new BigDecimal("120.0"))
                .bridgeWatchHoursNight(new BigDecimal("120.0"))
                .rankServed("DECK_CADET")
                .build();

        SeaServiceRecordDto created = seaService.createSeaServiceRecord(req);

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(SeaServiceStatus.IN_PROGRESS);
        assertThat(created.getVesselName()).isEqualTo("MV PACIFIC EXPLORER");
        assertThat(created.getDaysAtSea()).isEqualTo(60);
    }

    @Test
    void shouldDischargeSeaServiceRecord() {
        CreateSeaServiceRecordRequestDto req = CreateSeaServiceRecordRequestDto.builder()
                .tarBookId(tarBookId)
                .candidateId(candidateId)
                .vesselName("MV PACIFIC EXPLORER")
                .vesselImo("9876543")
                .flagState("PANAMA")
                .vesselType("CONTAINER")
                .grossTonnage(new BigDecimal("45000.00"))
                .signOnDate(LocalDate.now().minusMonths(3))
                .rankServed("DECK_CADET")
                .build();
        SeaServiceRecordDto created = seaService.createSeaServiceRecord(req);

        SeaServiceRecordDto discharged = seaService.dischargeSeaServiceRecord(created.getId());

        assertThat(discharged.getStatus()).isEqualTo(SeaServiceStatus.DISCHARGED);
    }

    @Test
    void shouldEndorseSeaServiceWithMasterSignoffAndSha256() {
        CreateSeaServiceRecordRequestDto req = CreateSeaServiceRecordRequestDto.builder()
                .tarBookId(tarBookId)
                .candidateId(candidateId)
                .vesselName("MV PACIFIC EXPLORER")
                .vesselImo("9876543")
                .flagState("PANAMA")
                .vesselType("CONTAINER")
                .grossTonnage(new BigDecimal("45000.00"))
                .signOnDate(LocalDate.now().minusMonths(3))
                .rankServed("DECK_CADET")
                .build();
        SeaServiceRecordDto created = seaService.createSeaServiceRecord(req);

        EndorseSeaServiceRecordRequestDto endorseReq = EndorseSeaServiceRecordRequestDto.builder()
                .endorserUserId(masterId)
                .endorserRole(EndorserRole.MASTER)
                .endorsementType(EndorsementType.FINAL_DISCHARGE)
                .conductRating("EXCELLENT")
                .abilityRating("EXCELLENT")
                .comments("Satisfactory sea service completed")
                .keyId("KEY-MASTER-001")
                .build();

        SeaServiceEndorsementDto endorsement = seaService.endorseSeaServiceRecord(created.getId(), endorseReq);

        assertThat(endorsement).isNotNull();
        assertThat(endorsement.getEndorserRole()).isEqualTo(EndorserRole.MASTER);
        assertThat(endorsement.getEndorsementType()).isEqualTo(EndorsementType.FINAL_DISCHARGE);
        assertThat(endorsement.getSignaturePayloadHash()).isNotBlank();
        assertThat(endorsement.getSignaturePayloadHash()).hasSize(64);
    }

    @Test
    void shouldPreventDuplicateFinalDischargeEndorsement() {
        CreateSeaServiceRecordRequestDto req = CreateSeaServiceRecordRequestDto.builder()
                .tarBookId(tarBookId)
                .candidateId(candidateId)
                .vesselName("MV PACIFIC EXPLORER")
                .vesselImo("9876543")
                .flagState("PANAMA")
                .vesselType("CONTAINER")
                .grossTonnage(new BigDecimal("45000.00"))
                .signOnDate(LocalDate.now().minusMonths(3))
                .rankServed("DECK_CADET")
                .build();
        SeaServiceRecordDto created = seaService.createSeaServiceRecord(req);

        EndorseSeaServiceRecordRequestDto endorseReq = EndorseSeaServiceRecordRequestDto.builder()
                .endorserUserId(masterId)
                .endorserRole(EndorserRole.MASTER)
                .endorsementType(EndorsementType.FINAL_DISCHARGE)
                .conductRating("EXCELLENT")
                .abilityRating("EXCELLENT")
                .keyId("KEY-MASTER-001")
                .build();

        seaService.endorseSeaServiceRecord(created.getId(), endorseReq);

        assertThatThrownBy(() -> seaService.endorseSeaServiceRecord(created.getId(), endorseReq))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FINAL_DISCHARGE");
    }
}
