package com.mralmostcool.tarbook.journal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJournalEntryRequestDto {
    @NotNull
    private UUID cadetUserId;
    private UUID vesselAssignmentId;
    @NotNull
    private LocalDate entryDate;
    private BigDecimal seaDaysLogged;
    private BigDecimal watchkeepingHours;
    private String cadetComments;
}
