package com.mralmostcool.tarbook.assessment.dto;

import com.mralmostcool.tarbook.assessment.internal.domain.SignOffRole;
import com.mralmostcool.tarbook.assessment.internal.domain.SignOffVerdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentSignOffDto {
    private UUID id;
    private UUID taskAssessmentId;
    private UUID signerUserId;
    private String signerName;
    private SignOffRole signerRole;
    private SignOffVerdict verdict;
    private String comments;
    private OffsetDateTime signedAtUtc;
}
