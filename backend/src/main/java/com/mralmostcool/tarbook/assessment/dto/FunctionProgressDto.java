package com.mralmostcool.tarbook.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionProgressDto {
    private UUID functionId;
    private String functionCode;
    private String functionTitle;
    private long totalTasks;
    private long completedTasks;
    private double completionPercentage;
}
