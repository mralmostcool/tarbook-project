package com.mralmostcool.tarbook.core.dto;

import com.mralmostcool.tarbook.core.internal.domain.TrainingStream;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class CandidateDto {
    private UUID id;
    private UUID sponsoringOrgId;
    private String indosNumber;
    private String cdcNumber;
    private TrainingStream trainingStream;
    private LocalDate dateOfBirth;
    private OffsetDateTime createdAtUtc;
    private OffsetDateTime updatedAtUtc;
}
