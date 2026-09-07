package com.mralmostcool.tarbook.assessment;

import com.mralmostcool.tarbook.assessment.dto.CreateAssessmentRequestDto;
import com.mralmostcool.tarbook.assessment.dto.ProgressReportDto;
import com.mralmostcool.tarbook.assessment.dto.SignOffAssessmentRequestDto;
import com.mralmostcool.tarbook.assessment.dto.TaskAssessmentDto;
import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentGrade;
import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentSignOff;
import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentStatus;
import com.mralmostcool.tarbook.assessment.internal.domain.SignOffRole;
import com.mralmostcool.tarbook.assessment.internal.domain.SignOffVerdict;
import com.mralmostcool.tarbook.assessment.internal.domain.TaskAssessment;
import com.mralmostcool.tarbook.assessment.internal.repository.AssessmentSignOffRepository;
import com.mralmostcool.tarbook.assessment.internal.repository.TaskAssessmentRepository;
import com.mralmostcool.tarbook.assessment.internal.service.AssessmentWorkflowInternalService;
import com.mralmostcool.tarbook.assessment.internal.service.ProgressTrackingInternalService;
import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.SystemRole;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.program.internal.domain.StcwProgram;
import com.mralmostcool.tarbook.program.internal.domain.SyllabusFunction;
import com.mralmostcool.tarbook.program.internal.domain.SyllabusTask;
import com.mralmostcool.tarbook.program.internal.repository.SyllabusTaskRepository;
import com.mralmostcool.tarbook.program.internal.service.ProgramSyllabusInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssessmentServiceUnitTest {

    private AssessmentService assessmentService;

    private List<TaskAssessment> assessmentStore;
    private List<AssessmentSignOff> signOffStore;

    private UUID tarBookId;
    private UUID candidateId;
    private UUID officerId;
    private UUID taskId;
    private UUID programId;
    private UUID functionId;

    private AppUser candidateUser;
    private AppUser officerUser;
    private SyllabusTask syllabusTask;
    private SyllabusFunction syllabusFunction;

    @BeforeEach
    void setUp() {
        tarBookId = UUID.randomUUID();
        candidateId = UUID.randomUUID();
        officerId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        programId = UUID.randomUUID();
        functionId = UUID.randomUUID();

        assessmentStore = new ArrayList<>();
        signOffStore = new ArrayList<>();

        candidateUser = AppUser.builder()
                .id(candidateId)
                .email("cadet.deck@maritime.org")
                .fullName("Cadet Deck")
                .systemRole(SystemRole.CANDIDATE)
                .build();

        officerUser = AppUser.builder()
                .id(officerId)
                .email("chief.officer@maritime.org")
                .fullName("Chief Officer")
                .systemRole(SystemRole.OFFICER)
                .build();

        StcwProgram program = StcwProgram.builder()
                .id(programId)
                .code("STCW-DECK-2026")
                .title("Deck Cadet Officer of the Watch")
                .build();

        syllabusFunction = SyllabusFunction.builder()
                .id(functionId)
                .program(program)
                .functionCode("NAV-01")
                .title("Navigation at Operational Level")
                .stcwCode("STCW II/1")
                .displayOrder(1)
                .build();

        syllabusTask = SyllabusTask.builder()
                .id(taskId)
                .function(syllabusFunction)
                .taskCode("NAV-01.1")
                .title("Plan and conduct a passage")
                .description("Execute passage planning according to SOLAS V/34")
                .requiredSignOffs(1)
                .build();

        AppUserInternalService userInternalService = new AppUserInternalService(null) {
            @Override
            public Optional<AppUser> findById(UUID id) {
                if (candidateId.equals(id)) return Optional.of(candidateUser);
                if (officerId.equals(id)) return Optional.of(officerUser);
                return Optional.empty();
            }
        };

        SyllabusTaskRepository syllabusTaskRepository = new SyllabusTaskRepository() {
            @Override
            public Optional<SyllabusTask> findById(UUID id) {
                if (taskId.equals(id)) return Optional.of(syllabusTask);
                return Optional.empty();
            }
            @Override public List<SyllabusTask> findByFunctionId(UUID functionId) { return List.of(syllabusTask); }
            @Override public <S extends SyllabusTask> S save(S entity) { return entity; }
            @Override public <S extends SyllabusTask> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public List<SyllabusTask> findAll() { return List.of(syllabusTask); }
            @Override public List<SyllabusTask> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return 1; }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(SyllabusTask entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends SyllabusTask> entities) {}
            @Override public void deleteAll() {}
            @Override public void flush() {}
            @Override public <S extends SyllabusTask> S saveAndFlush(S entity) { return null; }
            @Override public <S extends SyllabusTask> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<SyllabusTask> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public SyllabusTask getOne(UUID uuid) { return null; }
            @Override public SyllabusTask getById(UUID uuid) { return null; }
            @Override public SyllabusTask getReferenceById(UUID uuid) { return null; }
            @Override public <S extends SyllabusTask> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends SyllabusTask> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends SyllabusTask> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends SyllabusTask> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends SyllabusTask> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends SyllabusTask> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends SyllabusTask, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public List<SyllabusTask> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<SyllabusTask> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        TaskAssessmentRepository taskAssessmentRepository = new TaskAssessmentRepository() {
            @Override
            public TaskAssessment save(TaskAssessment entity) {
                assessmentStore.removeIf(a -> a.getId().equals(entity.getId()));
                assessmentStore.add(entity);
                return entity;
            }

            @Override
            public Optional<TaskAssessment> findById(UUID id) {
                return assessmentStore.stream().filter(a -> a.getId().equals(id)).findFirst();
            }

            @Override
            public List<TaskAssessment> findByCandidateUserId(UUID cId) {
                return assessmentStore.stream().filter(a -> a.getCandidateUser().getId().equals(cId)).toList();
            }

            @Override
            public List<TaskAssessment> findByTarBookId(UUID tbId) {
                return assessmentStore.stream().filter(a -> a.getTarBookId().equals(tbId)).toList();
            }

            @Override
            public Optional<TaskAssessment> findByTarBookIdAndTaskDefinitionId(UUID tbId, UUID tdId) {
                return assessmentStore.stream().filter(a -> a.getTarBookId().equals(tbId) && a.getTaskDefinition().getId().equals(tdId)).findFirst();
            }

            @Override
            public long countByTarBookId(UUID tbId) {
                return assessmentStore.stream().filter(a -> a.getTarBookId().equals(tbId)).count();
            }

            @Override
            public long countByTarBookIdAndStatus(UUID tbId, AssessmentStatus status) {
                return assessmentStore.stream().filter(a -> a.getTarBookId().equals(tbId) && a.getStatus() == status).count();
            }

            @Override
            public long countByTarBookIdAndTaskDefinitionFunctionId(UUID tbId, UUID fId) {
                return assessmentStore.stream().filter(a -> a.getTarBookId().equals(tbId) && a.getTaskDefinition().getFunction().getId().equals(fId)).count();
            }

            @Override
            public long countByTarBookIdAndTaskDefinitionFunctionIdAndStatus(UUID tbId, UUID fId, AssessmentStatus status) {
                return assessmentStore.stream().filter(a -> a.getTarBookId().equals(tbId) && a.getTaskDefinition().getFunction().getId().equals(fId) && a.getStatus() == status).count();
            }

            @Override public <S extends TaskAssessment> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public List<TaskAssessment> findAll() { return assessmentStore; }
            @Override public List<TaskAssessment> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return assessmentStore.size(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(TaskAssessment entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends TaskAssessment> entities) {}
            @Override public void deleteAll() {}
            @Override public void flush() {}
            @Override public <S extends TaskAssessment> S saveAndFlush(S entity) { return null; }
            @Override public <S extends TaskAssessment> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<TaskAssessment> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public TaskAssessment getOne(UUID uuid) { return null; }
            @Override public TaskAssessment getById(UUID uuid) { return null; }
            @Override public TaskAssessment getReferenceById(UUID uuid) { return null; }
            @Override public <S extends TaskAssessment> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends TaskAssessment> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends TaskAssessment> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends TaskAssessment> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends TaskAssessment> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends TaskAssessment> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends TaskAssessment, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public List<TaskAssessment> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<TaskAssessment> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        AssessmentSignOffRepository assessmentSignOffRepository = new AssessmentSignOffRepository() {
            @Override
            public AssessmentSignOff save(AssessmentSignOff entity) {
                signOffStore.add(entity);
                return entity;
            }

            @Override
            public List<AssessmentSignOff> findByTaskAssessmentIdOrderBySignedAtUtcAsc(UUID taId) {
                return signOffStore.stream().filter(so -> so.getTaskAssessment().getId().equals(taId)).toList();
            }

            @Override public <S extends AssessmentSignOff> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public Optional<AssessmentSignOff> findById(UUID uuid) { return Optional.empty(); }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public List<AssessmentSignOff> findAll() { return signOffStore; }
            @Override public List<AssessmentSignOff> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return signOffStore.size(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(AssessmentSignOff entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends AssessmentSignOff> entities) {}
            @Override public void deleteAll() {}
            @Override public void flush() {}
            @Override public <S extends AssessmentSignOff> S saveAndFlush(S entity) { return null; }
            @Override public <S extends AssessmentSignOff> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<AssessmentSignOff> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public AssessmentSignOff getOne(UUID uuid) { return null; }
            @Override public AssessmentSignOff getById(UUID uuid) { return null; }
            @Override public AssessmentSignOff getReferenceById(UUID uuid) { return null; }
            @Override public <S extends AssessmentSignOff> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends AssessmentSignOff> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends AssessmentSignOff> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends AssessmentSignOff> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends AssessmentSignOff> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends AssessmentSignOff> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends AssessmentSignOff, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public List<AssessmentSignOff> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<AssessmentSignOff> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        };

        ProgramSyllabusInternalService syllabusInternalService = new ProgramSyllabusInternalService(null, null, null) {
            @Override
            public List<SyllabusFunction> findFunctionsByProgramId(UUID pId) {
                if (programId.equals(pId)) return List.of(syllabusFunction);
                return List.of();
            }
        };

        AssessmentWorkflowInternalService workflowService = new AssessmentWorkflowInternalService(
                taskAssessmentRepository,
                assessmentSignOffRepository,
                userInternalService,
                syllabusTaskRepository
        );

        ProgressTrackingInternalService progressService = new ProgressTrackingInternalService(
                taskAssessmentRepository,
                syllabusInternalService
        );

        assessmentService = new AssessmentService(workflowService, progressService);
    }

    @Test
    void shouldCreateTaskAssessmentInPendingStatus() {
        CreateAssessmentRequestDto request = CreateAssessmentRequestDto.builder()
                .tarBookId(tarBookId)
                .candidateUserId(candidateId)
                .taskDefinitionId(taskId)
                .grade(AssessmentGrade.SATISFACTORY)
                .comments("Passage plan reviewed and demonstrated correctly")
                .build();

        TaskAssessmentDto created = assessmentService.createAssessment(request);

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(AssessmentStatus.PENDING);
        assertThat(created.getGrade()).isEqualTo(AssessmentGrade.SATISFACTORY);
        assertThat(created.getCandidateUserId()).isEqualTo(candidateId);
        assertThat(created.getTaskCode()).isEqualTo("NAV-01.1");
    }

    @Test
    void shouldPerformSignOffWithApproval() {
        CreateAssessmentRequestDto req = CreateAssessmentRequestDto.builder()
                .tarBookId(tarBookId)
                .candidateUserId(candidateId)
                .taskDefinitionId(taskId)
                .grade(AssessmentGrade.EXCELLENT)
                .comments("Flawless execution")
                .build();
        TaskAssessmentDto created = assessmentService.createAssessment(req);

        SignOffAssessmentRequestDto signOffReq = SignOffAssessmentRequestDto.builder()
                .signerUserId(officerId)
                .signerRole(SignOffRole.CHIEF_OFFICER)
                .verdict(SignOffVerdict.APPROVED)
                .comments("Approved by Chief Officer")
                .build();

        TaskAssessmentDto signedOff = assessmentService.signOffAssessment(created.getId(), signOffReq);

        assertThat(signedOff.getStatus()).isEqualTo(AssessmentStatus.APPROVED);
        assertThat(signedOff.getSignOffs()).hasSize(1);
        assertThat(signedOff.getSignOffs().get(0).getSignerRole()).isEqualTo(SignOffRole.CHIEF_OFFICER);
        assertThat(signedOff.getSignOffs().get(0).getVerdict()).isEqualTo(SignOffVerdict.APPROVED);
    }

    @Test
    void shouldPerformSignOffWithReworkRequest() {
        CreateAssessmentRequestDto req = CreateAssessmentRequestDto.builder()
                .tarBookId(tarBookId)
                .candidateUserId(candidateId)
                .taskDefinitionId(taskId)
                .grade(AssessmentGrade.NEEDS_IMPROVEMENT)
                .comments("Needs re-calculation")
                .build();
        TaskAssessmentDto created = assessmentService.createAssessment(req);

        SignOffAssessmentRequestDto signOffReq = SignOffAssessmentRequestDto.builder()
                .signerUserId(officerId)
                .signerRole(SignOffRole.ASSESSOR)
                .verdict(SignOffVerdict.REWORK_REQUESTED)
                .comments("Re-do chart work calculation")
                .build();

        TaskAssessmentDto signedOff = assessmentService.signOffAssessment(created.getId(), signOffReq);

        assertThat(signedOff.getStatus()).isEqualTo(AssessmentStatus.REWORK_REQUESTED);
        assertThat(signedOff.getSignOffs()).hasSize(1);
        assertThat(signedOff.getSignOffs().get(0).getVerdict()).isEqualTo(SignOffVerdict.REWORK_REQUESTED);
    }

    @Test
    void shouldCalculateProgressReport() {
        CreateAssessmentRequestDto req = CreateAssessmentRequestDto.builder()
                .tarBookId(tarBookId)
                .candidateUserId(candidateId)
                .taskDefinitionId(taskId)
                .grade(AssessmentGrade.EXCELLENT)
                .build();
        TaskAssessmentDto created = assessmentService.createAssessment(req);

        SignOffAssessmentRequestDto signOffReq = SignOffAssessmentRequestDto.builder()
                .signerUserId(officerId)
                .signerRole(SignOffRole.CHIEF_OFFICER)
                .verdict(SignOffVerdict.APPROVED)
                .build();
        assessmentService.signOffAssessment(created.getId(), signOffReq);

        ProgressReportDto progress = assessmentService.getProgressReport(tarBookId, programId);

        assertThat(progress).isNotNull();
        assertThat(progress.getTotalTasks()).isEqualTo(1);
        assertThat(progress.getCompletedTasks()).isEqualTo(1);
        assertThat(progress.getOverallCompletionPercentage()).isEqualTo(100.0);
        assertThat(progress.getFunctionBreakdown()).hasSize(1);
        assertThat(progress.getFunctionBreakdown().get(0).getFunctionCode()).isEqualTo("NAV-01");
        assertThat(progress.getFunctionBreakdown().get(0).getCompletionPercentage()).isEqualTo(100.0);
    }
}
