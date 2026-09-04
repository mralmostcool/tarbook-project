package com.mralmostcool.tarbook.program.dto;

import com.mralmostcool.tarbook.core.internal.domain.TrainingStream;
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
public class ProgramSyllabusDto {
    private UUID id;
    private String code;
    private String title;
    private TrainingStream stream;
    private Integer totalRequiredSeaDays;
    private OffsetDateTime createdAtUtc;
}
