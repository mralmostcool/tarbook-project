package com.mralmostcool.tarbook.seaservice;

import com.mralmostcool.tarbook.seaservice.dto.CreateSeaServiceRecordRequestDto;
import com.mralmostcool.tarbook.seaservice.dto.EndorseSeaServiceRecordRequestDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceEndorsementDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceRecordDto;
import com.mralmostcool.tarbook.seaservice.dto.SeaServiceSummaryDto;
import com.mralmostcool.tarbook.seaservice.internal.service.MasterEndorsementInternalService;
import com.mralmostcool.tarbook.seaservice.internal.service.SeaServiceRecordInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeaService {

    private final SeaServiceRecordInternalService recordInternalService;
    private final MasterEndorsementInternalService endorsementInternalService;

    @Transactional
    public SeaServiceRecordDto createSeaServiceRecord(CreateSeaServiceRecordRequestDto request) {
        return recordInternalService.createRecord(request);
    }

    @Transactional
    public SeaServiceRecordDto dischargeSeaServiceRecord(UUID recordId) {
        return recordInternalService.dischargeRecord(recordId);
    }

    @Transactional
    public SeaServiceEndorsementDto endorseSeaServiceRecord(UUID recordId, EndorseSeaServiceRecordRequestDto request) {
        return endorsementInternalService.endorseSeaService(recordId, request);
    }

    @Transactional(readOnly = true)
    public List<SeaServiceRecordDto> getCandidateSeaServiceRecords(UUID candidateId) {
        return recordInternalService.getCandidateRecords(candidateId);
    }

    @Transactional(readOnly = true)
    public Optional<SeaServiceRecordDto> getSeaServiceRecordById(UUID recordId) {
        return recordInternalService.getRecordById(recordId);
    }

    @Transactional(readOnly = true)
    public SeaServiceSummaryDto getCandidateSeaServiceSummary(UUID candidateId) {
        return recordInternalService.calculateSummary(candidateId);
    }
}
