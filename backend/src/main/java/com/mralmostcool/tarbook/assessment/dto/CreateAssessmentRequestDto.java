package com.mralmostcool.tarbook.assessment.dto;

import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssessmentRequestDto {
    private UUID tarBookId;
    private UUID taskDefinitionId;
    private UUID candidateUserId;
    private AssessmentGrade grade;
    private String comments;
}
