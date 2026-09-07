package com.mralmostcool.tarbook.seaservice;

import com.mralmostcool.tarbook.seaservice.dto.CreateSeaServiceRecordRequestDto;
import com.mralmostcool.tarbook.seaservice.dto.EndorseSeaServiceRecordRequestDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceEndorsementDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceRecordDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sea-service-records")
@RequiredArgsConstructor
@Tag(name = "Sea Service & Endorsements", description = "Endpoints for maritime sea service voyage logs, watchkeeping hours, and Master statutory endorsements")
public class SeaServiceController {

    private final SeaService seaService;

    @PostMapping
    @Operation(summary = "Create sea service record")
    public ResponseEntity<SeaServiceRecordDto> createRecord(@Valid @RequestBody CreateSeaServiceRecordRequestDto request) {
        return ResponseEntity.ok(seaService.createSeaServiceRecord(request));
    }

    @PostMapping("/{id}/discharge")
    @Operation(summary = "Discharge candidate from sea service voyage")
    public ResponseEntity<SeaServiceRecordDto> dischargeRecord(@PathVariable UUID id) {
        return ResponseEntity.ok(seaService.dischargeSeaServiceRecord(id));
    }

    @PostMapping("/{id}/endorse")
    @Operation(summary = "Endorse sea service record with Master/Chief Engineer statutory sign-off")
    public ResponseEntity<SeaServiceEndorsementDto> endorseRecord(
            @PathVariable UUID id,
            @Valid @RequestBody EndorseSeaServiceRecordRequestDto request) {
        return ResponseEntity.ok(seaService.endorseSeaServiceRecord(id, request));
    }

    @GetMapping("/candidate/{candidateId}")
    @Operation(summary = "Get all sea service records for candidate")
    public ResponseEntity<List<SeaServiceRecordDto>> getCandidateRecords(@PathVariable UUID candidateId) {
        return ResponseEntity.ok(seaService.getCandidateSeaServiceRecords(candidateId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sea service record by ID")
    public ResponseEntity<SeaServiceRecordDto> getRecordById(@PathVariable UUID id) {
        return seaService.getSeaServiceRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/candidate/{candidateId}/summary")
    @Operation(summary = "Get certified sea service and watchkeeping hours summary")
    public ResponseEntity<SeaServiceSummaryDto> getCandidateSummary(@PathVariable UUID candidateId) {
        return ResponseEntity.ok(seaService.getCandidateSeaServiceSummary(candidateId));
    }
}
