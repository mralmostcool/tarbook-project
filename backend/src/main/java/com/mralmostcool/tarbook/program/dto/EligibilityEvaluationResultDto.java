package com.mralmostcool.tarbook.program.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityEvaluationResultDto {
    private UUID programId;
    private boolean eligible;
    private int actualSeaDays;
    private int requiredSeaDays;
    private int actualWatchkeepingHours;
    private String statusMessage;
}
