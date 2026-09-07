package com.mralmostcool.tarbook.assessment;

import com.mralmostcool.tarbook.assessment.dto.CreateAssessmentRequestDto;
import com.mralmostcool.tarbook.assessment.dto.ProgressReportDto;
import com.mralmostcool.tarbook.assessment.dto.SignOffAssessmentRequestDto;
import com.mralmostcool.tarbook.assessment.dto.TaskAssessmentDto;
import com.mralmostcool.tarbook.assessment.internal.service.AssessmentWorkflowInternalService;
import com.mralmostcool.tarbook.assessment.internal.service.ProgressTrackingInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentWorkflowInternalService workflowInternalService;
    private final ProgressTrackingInternalService progressTrackingInternalService;

    @Transactional
    public TaskAssessmentDto createAssessment(CreateAssessmentRequestDto request) {
        return workflowInternalService.createTaskAssessment(request);
    }

    @Transactional
    public TaskAssessmentDto signOffAssessment(UUID assessmentId, SignOffAssessmentRequestDto request) {
        return workflowInternalService.performSignOff(assessmentId, request);
    }

    @Transactional(readOnly = true)
    public List<TaskAssessmentDto> getCandidateAssessments(UUID candidateUserId) {
        return workflowInternalService.getAssessmentsForCandidate(candidateUserId);
    }

    @Transactional(readOnly = true)
    public Optional<TaskAssessmentDto> getAssessmentById(UUID id) {
        return workflowInternalService.getAssessmentById(id);
    }

    @Transactional(readOnly = true)
    public ProgressReportDto getProgressReport(UUID tarBookId, UUID programId) {
        return progressTrackingInternalService.calculateProgress(tarBookId, programId);
    }
}
