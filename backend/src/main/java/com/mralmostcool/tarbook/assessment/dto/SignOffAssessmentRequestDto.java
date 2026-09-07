package com.mralmostcool.tarbook.assessment.dto;

import com.mralmostcool.tarbook.assessment.internal.domain.SignOffRole;
import com.mralmostcool.tarbook.assessment.internal.domain.SignOffVerdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignOffAssessmentRequestDto {
    private UUID signerUserId;
    private SignOffRole signerRole;
    private SignOffVerdict verdict;
    private String comments;
}
