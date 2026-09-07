package com.mralmostcool.tarbook.seaservice.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
@Table(name = "sea_service_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeaServiceRecord {

    @Id
    private UUID id;

    @Column(name = "tar_book_id", nullable = false)
    private UUID tarBookId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "vessel_name", nullable = false)
    private String vesselName;

    @Column(name = "vessel_imo", nullable = false, length = 10)
    private String vesselImo;

    @Column(name = "flag_state", nullable = false, length = 100)
    private String flagState;

    @Column(name = "vessel_type", nullable = false, length = 100)
    private String vesselType;

    @Column(name = "gross_tonnage", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossTonnage;

    @Column(name = "engine_power_kw", precision = 10, scale = 2)
    private BigDecimal enginePowerKw;

    @Column(name = "sign_on_date", nullable = false)
    private LocalDate signOnDate;

    @Column(name = "sign_off_date")
    private LocalDate signOffDate;

    @Column(name = "days_at_sea", nullable = false)
    private Integer daysAtSea;

    @Column(name = "days_in_port", nullable = false)
    private Integer daysInPort;

    @Column(name = "bridge_watch_hours_day", nullable = false, precision = 8, scale = 2)
    private BigDecimal bridgeWatchHoursDay;

    @Column(name = "bridge_watch_hours_night", nullable = false, precision = 8, scale = 2)
    private BigDecimal bridgeWatchHoursNight;

    @Column(name = "engine_watch_hours_day", nullable = false, precision = 8, scale = 2)
    private BigDecimal engineWatchHoursDay;

    @Column(name = "engine_watch_hours_night", nullable = false, precision = 8, scale = 2)
    private BigDecimal engineWatchHoursNight;

    @Column(name = "steering_hours", nullable = false, precision = 8, scale = 2)
    private BigDecimal steeringHours;

    @Column(name = "rank_served", nullable = false, length = 50)
    private String rankServed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SeaServiceStatus status;

    @Column(name = "created_at_utc", nullable = false, updatable = false)
    private OffsetDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false)
    private OffsetDateTime updatedAtUtc;
}
