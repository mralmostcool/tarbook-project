package com.mralmostcool.tarbook.assessment;

import com.mralmostcool.tarbook.assessment.dto.CreateAssessmentRequestDto;
import com.mralmostcool.tarbook.assessment.dto.ProgressReportDto;
import com.mralmostcool.tarbook.assessment.dto.SignOffAssessmentRequestDto;
import com.mralmostcool.tarbook.assessment.dto.TaskAssessmentDto;
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
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
@Tag(name = "Assessment & Sign-Off Workflow", description = "Endpoints for task assessment, officer sign-off hierarchy, and progress tracking")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping
    @Operation(summary = "Create task assessment")
    public ResponseEntity<TaskAssessmentDto> createAssessment(@Valid @RequestBody CreateAssessmentRequestDto request) {
        return ResponseEntity.ok(assessmentService.createAssessment(request));
    }

    @PostMapping("/{id}/sign-off")
    @Operation(summary = "Sign off task assessment")
    public ResponseEntity<TaskAssessmentDto> signOffAssessment(
            @PathVariable UUID id,
            @Valid @RequestBody SignOffAssessmentRequestDto request) {
        return ResponseEntity.ok(assessmentService.signOffAssessment(id, request));
    }

    @GetMapping("/candidate/{candidateUserId}")
    @Operation(summary = "Get assessments for candidate")
    public ResponseEntity<List<TaskAssessmentDto>> getCandidateAssessments(@PathVariable UUID candidateUserId) {
        return ResponseEntity.ok(assessmentService.getCandidateAssessments(candidateUserId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task assessment by ID")
    public ResponseEntity<TaskAssessmentDto> getAssessmentById(@PathVariable UUID id) {
        return assessmentService.getAssessmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/progress/{tarBookId}")
    @Operation(summary = "Get progress report for TAR Book")
    public ResponseEntity<ProgressReportDto> getProgressReport(
            @PathVariable UUID tarBookId,
            @RequestParam(required = false) UUID programId) {
        return ResponseEntity.ok(assessmentService.getProgressReport(tarBookId, programId));
    }
}
