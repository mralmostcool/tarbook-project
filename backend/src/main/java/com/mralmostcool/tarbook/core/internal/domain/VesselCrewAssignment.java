package com.mralmostcool.tarbook.core.internal.domain;

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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vessel_crew_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VesselCrewAssignment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsoring_org_id", nullable = false)
    private Organization sponsoringOrganization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_user_id", nullable = false)
    private AppUser officerUser;

    @Column(name = "external_assignment_id", length = 100)
    private String externalAssignmentId;

    @Column(name = "vessel_name", nullable = false)
    private String vesselName;

    @Column(name = "vessel_imo", nullable = false, length = 10)
    private String vesselImo;

    @Column(nullable = false, length = 50)
    private String rank;

    @Column(name = "sign_on_date", nullable = false)
    private LocalDate signOnDate;

    @Column(name = "sign_off_date")
    private LocalDate signOffDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssignmentStatus status;

    @Column(name = "sync_sequence")
    private Long syncSequence;

    @Column(name = "created_at_utc", nullable = false, updatable = false)
    private OffsetDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false)
    private OffsetDateTime updatedAtUtc;
}
