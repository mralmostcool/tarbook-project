package com.mralmostcool.tarbook.assessment.internal.service;

import com.mralmostcool.tarbook.assessment.dto.AssessmentSignOffDto;
import com.mralmostcool.tarbook.assessment.dto.CreateAssessmentRequestDto;
import com.mralmostcool.tarbook.assessment.dto.SignOffAssessmentRequestDto;
import com.mralmostcool.tarbook.assessment.dto.TaskAssessmentDto;
import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentSignOff;
import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentStatus;
import com.mralmostcool.tarbook.assessment.internal.domain.SignOffVerdict;
import com.mralmostcool.tarbook.assessment.internal.domain.TaskAssessment;
import com.mralmostcool.tarbook.assessment.internal.repository.AssessmentSignOffRepository;
import com.mralmostcool.tarbook.assessment.internal.repository.TaskAssessmentRepository;
import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.program.internal.domain.SyllabusTask;
import com.mralmostcool.tarbook.program.internal.repository.SyllabusTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentWorkflowInternalService {

    private final TaskAssessmentRepository taskAssessmentRepository;
    private final AssessmentSignOffRepository assessmentSignOffRepository;
    private final AppUserInternalService userInternalService;
    private final SyllabusTaskRepository syllabusTaskRepository;

    @Transactional
    public TaskAssessmentDto createTaskAssessment(CreateAssessmentRequestDto request) {
        AppUser candidate = userInternalService.findById(request.getCandidateUserId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate user not found: " + request.getCandidateUserId()));

        SyllabusTask task = syllabusTaskRepository.findById(request.getTaskDefinitionId())
                .orElseThrow(() -> new IllegalArgumentException("Syllabus task not found: " + request.getTaskDefinitionId()));

        OffsetDateTime now = OffsetDateTime.now();
        TaskAssessment assessment = TaskAssessment.builder()
                .id(UUID.randomUUID())
                .tarBookId(request.getTarBookId())
                .taskDefinition(task)
                .candidateUser(candidate)
                .grade(request.getGrade())
                .status(AssessmentStatus.PENDING)
                .comments(request.getComments())
                .createdAtUtc(now)
                .updatedAtUtc(now)
                .build();

        TaskAssessment saved = taskAssessmentRepository.save(assessment);
        return mapToDto(saved);
    }

    @Transactional
    public TaskAssessmentDto performSignOff(UUID assessmentId, SignOffAssessmentRequestDto request) {
        TaskAssessment assessment = taskAssessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Task assessment not found: " + assessmentId));

        AppUser signer = userInternalService.findById(request.getSignerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Signer user not found: " + request.getSignerUserId()));

        OffsetDateTime now = OffsetDateTime.now();
        AssessmentSignOff signOff = AssessmentSignOff.builder()
                .id(UUID.randomUUID())
                .taskAssessment(assessment)
                .signerUser(signer)
                .signerRole(request.getSignerRole())
                .verdict(request.getVerdict())
                .comments(request.getComments())
                .signedAtUtc(now)
                .build();

        assessmentSignOffRepository.save(signOff);

        if (request.getVerdict() == SignOffVerdict.APPROVED) {
            assessment.setStatus(AssessmentStatus.APPROVED);
        } else if (request.getVerdict() == SignOffVerdict.REJECTED) {
            assessment.setStatus(AssessmentStatus.REJECTED);
        } else if (request.getVerdict() == SignOffVerdict.REWORK_REQUESTED) {
            assessment.setStatus(AssessmentStatus.REWORK_REQUESTED);
        }

        assessment.setUpdatedAtUtc(now);
        TaskAssessment updated = taskAssessmentRepository.save(assessment);
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<TaskAssessmentDto> getAssessmentsForCandidate(UUID candidateUserId) {
        return taskAssessmentRepository.findByCandidateUserId(candidateUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TaskAssessmentDto> getAssessmentById(UUID id) {
        return taskAssessmentRepository.findById(id).map(this::mapToDto);
    }

    public TaskAssessmentDto mapToDto(TaskAssessment assessment) {
        List<AssessmentSignOffDto> signOffDtos = assessmentSignOffRepository
                .findByTaskAssessmentIdOrderBySignedAtUtcAsc(assessment.getId()).stream()
                .map(so -> AssessmentSignOffDto.builder()
                        .id(so.getId())
                        .taskAssessmentId(so.getTaskAssessment().getId())
                        .signerUserId(so.getSignerUser().getId())
                        .signerName(so.getSignerUser().getFullName())
                        .signerRole(so.getSignerRole())
                        .verdict(so.getVerdict())
                        .comments(so.getComments())
                        .signedAtUtc(so.getSignedAtUtc())
                        .build())
                .collect(Collectors.toList());

        return TaskAssessmentDto.builder()
                .id(assessment.getId())
                .tarBookId(assessment.getTarBookId())
                .taskDefinitionId(assessment.getTaskDefinition().getId())
                .taskCode(assessment.getTaskDefinition().getTaskCode())
                .taskTitle(assessment.getTaskDefinition().getTitle())
                .candidateUserId(assessment.getCandidateUser().getId())
                .grade(assessment.getGrade())
                .status(assessment.getStatus())
                .comments(assessment.getComments())
                .signOffs(signOffDtos)
                .createdAtUtc(assessment.getCreatedAtUtc())
                .updatedAtUtc(assessment.getUpdatedAtUtc())
                .build();
    }
}
