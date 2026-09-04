package com.mralmostcool.tarbook.journal.internal.domain;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.domain.VesselCrewAssignment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "journal_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cadet_user_id", nullable = false)
    private AppUser cadetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_assignment_id")
    private VesselCrewAssignment vesselAssignment;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "sea_days_logged", precision = 5, scale = 2)
    private BigDecimal seaDaysLogged;

    @Column(name = "watchkeeping_hours", precision = 5, scale = 2)
    private BigDecimal watchkeepingHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private JournalEntryStatus status;

    @Column(name = "cadet_comments", columnDefinition = "TEXT")
    private String cadetComments;

    @Column(name = "supervisor_comments", columnDefinition = "TEXT")
    private String supervisorComments;

    @Column(name = "created_at_utc", nullable = false, updatable = false)
    private OffsetDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false)
    private OffsetDateTime updatedAtUtc;
}
