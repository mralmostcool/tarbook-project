package com.mralmostcool.tarbook.seaservice.internal.service;

import com.mralmostcool.tarbook.seaservice.dto.CreateSeaServiceRecordRequestDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceEndorsementDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceRecordDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceSummaryDto;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceRecord;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceStatus;
import com.mralmostcool.tarbook.seaservice.internal.repository.SeaServiceEndorsementRepository;
import com.mralmostcool.tarbook.seaservice.internal.repository.SeaServiceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeaServiceRecordInternalService {

    private final SeaServiceRecordRepository recordRepository;
    private final SeaServiceEndorsementRepository endorsementRepository;

    @Transactional
    public SeaServiceRecordDto createRecord(CreateSeaServiceRecordRequestDto request) {
        if (request.getSignOffDate() != null && request.getSignOffDate().isBefore(request.getSignOnDate())) {
            throw new IllegalArgumentException("Sign-off date cannot be before sign-on date");
        }

        OffsetDateTime now = OffsetDateTime.now();
        SeaServiceRecord record = SeaServiceRecord.builder()
                .id(UUID.randomUUID())
                .tarBookId(request.getTarBookId())
                .candidateId(request.getCandidateId())
                .vesselName(request.getVesselName())
                .vesselImo(request.getVesselImo())
                .flagState(request.getFlagState())
                .vesselType(request.getVesselType())
                .grossTonnage(request.getGrossTonnage())
                .enginePowerKw(request.getEnginePowerKw())
                .signOnDate(request.getSignOnDate())
                .signOffDate(request.getSignOffDate())
                .daysAtSea(request.getDaysAtSea() != null ? request.getDaysAtSea() : 0)
                .daysInPort(request.getDaysInPort() != null ? request.getDaysInPort() : 0)
                .bridgeWatchHoursDay(request.getBridgeWatchHoursDay() != null ? request.getBridgeWatchHoursDay() : BigDecimal.ZERO)
                .bridgeWatchHoursNight(request.getBridgeWatchHoursNight() != null ? request.getBridgeWatchHoursNight() : BigDecimal.ZERO)
                .engineWatchHoursDay(request.getEngineWatchHoursDay() != null ? request.getEngineWatchHoursDay() : BigDecimal.ZERO)
                .engineWatchHoursNight(request.getEngineWatchHoursNight() != null ? request.getEngineWatchHoursNight() : BigDecimal.ZERO)
                .steeringHours(request.getSteeringHours() != null ? request.getSteeringHours() : BigDecimal.ZERO)
                .rankServed(request.getRankServed())
                .status(SeaServiceStatus.IN_PROGRESS)
                .createdAtUtc(now)
                .updatedAtUtc(now)
                .build();

        SeaServiceRecord saved = recordRepository.save(record);
        return mapToDto(saved);
    }

    @Transactional
    public SeaServiceRecordDto dischargeRecord(UUID recordId) {
        SeaServiceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Sea service record not found: " + recordId));

        record.setStatus(SeaServiceStatus.DISCHARGED);
        record.setUpdatedAtUtc(OffsetDateTime.now());

        SeaServiceRecord updated = recordRepository.save(record);
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<SeaServiceRecordDto> getCandidateRecords(UUID candidateId) {
        return recordRepository.findByCandidateId(candidateId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SeaServiceRecordDto> getRecordById(UUID recordId) {
        return recordRepository.findById(recordId).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public SeaServiceSummaryDto calculateSummary(UUID candidateId) {
        List<SeaServiceRecord> records = recordRepository.findByCandidateId(candidateId).stream()
                .filter(r -> r.getStatus() != SeaServiceStatus.VOIDED)
                .toList();

        int totalDaysAtSea = records.stream().mapToInt(r -> r.getDaysAtSea()).sum();
        int totalDaysInPort = records.stream().mapToInt(r -> r.getDaysInPort()).sum();

        BigDecimal totalBridgeWatch = records.stream()
                .map(r -> r.getBridgeWatchHoursDay().add(r.getBridgeWatchHoursNight()))
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        BigDecimal totalEngineWatch = records.stream()
                .map(r -> r.getEngineWatchHoursDay().add(r.getEngineWatchHoursNight()))
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        BigDecimal totalSteering = records.stream()
                .map(r -> r.getSteeringHours())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        return SeaServiceSummaryDto.builder()
                .candidateId(candidateId)
                .totalVoyages(records.size())
                .totalDaysAtSea(totalDaysAtSea)
                .totalDaysInPort(totalDaysInPort)
                .totalBridgeWatchHours(totalBridgeWatch)
                .totalEngineWatchHours(totalEngineWatch)
                .totalSteeringHours(totalSteering)
                .build();
    }

    public SeaServiceRecordDto mapToDto(SeaServiceRecord record) {
        List<SeaServiceEndorsementDto> endorsementDtos = endorsementRepository
                .findBySeaServiceRecordId(record.getId()).stream()
                .map(e -> SeaServiceEndorsementDto.builder()
                        .id(e.getId())
                        .seaServiceId(e.getSeaServiceRecord().getId())
                        .endorserUserId(e.getEndorserUser().getId())
                        .endorserName(e.getEndorserUser().getFullName())
                        .endorserRole(e.getEndorserRole())
                        .endorsementType(e.getEndorsementType())
                        .conductRating(e.getConductRating())
                        .abilityRating(e.getAbilityRating())
                        .comments(e.getComments())
                        .keyId(e.getKeyId())
                        .signaturePayloadHash(e.getSignaturePayloadHash())
                        .signedAtUtc(e.getSignedAtUtc())
                        .build())
                .collect(Collectors.toList());

        return SeaServiceRecordDto.builder()
                .id(record.getId())
                .tarBookId(record.getTarBookId())
                .candidateId(record.getCandidateId())
                .vesselName(record.getVesselName())
                .vesselImo(record.getVesselImo())
                .flagState(record.getFlagState())
                .vesselType(record.getVesselType())
                .grossTonnage(record.getGrossTonnage())
                .enginePowerKw(record.getEnginePowerKw())
                .signOnDate(record.getSignOnDate())
                .signOffDate(record.getSignOffDate())
                .daysAtSea(record.getDaysAtSea())
                .daysInPort(record.getDaysInPort())
                .bridgeWatchHoursDay(record.getBridgeWatchHoursDay())
                .bridgeWatchHoursNight(record.getBridgeWatchHoursNight())
                .engineWatchHoursDay(record.getEngineWatchHoursDay())
                .engineWatchHoursNight(record.getEngineWatchHoursNight())
                .steeringHours(record.getSteeringHours())
                .rankServed(record.getRankServed())
                .status(record.getStatus())
                .endorsements(endorsementDtos)
                .createdAtUtc(record.getCreatedAtUtc())
                .updatedAtUtc(record.getUpdatedAtUtc())
                .build();
    }
}
