package com.mralmostcool.tarbook.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressReportDto {
    private UUID tarBookId;
    private long totalTasks;
    private long completedTasks;
    private double overallCompletionPercentage;
    private List<FunctionProgressDto> functionBreakdown;
}
