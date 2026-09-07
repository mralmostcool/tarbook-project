package com.mralmostcool.tarbook.seaservice.dto;

import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeaServiceRecordDto {
    private UUID id;
    private UUID tarBookId;
    private UUID candidateId;
    private String vesselName;
    private String vesselImo;
    private String flagState;
    private String vesselType;
    private BigDecimal grossTonnage;
    private BigDecimal enginePowerKw;
    private LocalDate signOnDate;
    private LocalDate signOffDate;
    private Integer daysAtSea;
    private Integer daysInPort;
    private BigDecimal bridgeWatchHoursDay;
    private BigDecimal bridgeWatchHoursNight;
    private BigDecimal engineWatchHoursDay;
    private BigDecimal engineWatchHoursNight;
    private BigDecimal steeringHours;
    private String rankServed;
    private SeaServiceStatus status;
    private List<SeaServiceEndorsementDto> endorsements;
    private OffsetDateTime createdAtUtc;
    private OffsetDateTime updatedAtUtc;
}
