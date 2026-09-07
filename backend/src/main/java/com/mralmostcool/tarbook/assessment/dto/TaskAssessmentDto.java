package com.mralmostcool.tarbook.assessment.dto;

import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentGrade;
import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssessmentDto {
    private UUID id;
    private UUID tarBookId;
    private UUID taskDefinitionId;
    private String taskCode;
    private String taskTitle;
    private UUID candidateUserId;
    private AssessmentGrade grade;
    private AssessmentStatus status;
    private String comments;
    private List<AssessmentSignOffDto> signOffs;
    private OffsetDateTime createdAtUtc;
    private OffsetDateTime updatedAtUtc;
}
