package com.mralmostcool.tarbook.journal.dto;

import com.mralmostcool.tarbook.journal.internal.domain.JournalEntryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryDto {
    private UUID id;
    private UUID cadetUserId;
    private UUID vesselAssignmentId;
    private LocalDate entryDate;
    private BigDecimal seaDaysLogged;
    private BigDecimal watchkeepingHours;
    private JournalEntryStatus status;
    private String cadetComments;
    private String supervisorComments;
    private OffsetDateTime createdAtUtc;
}
