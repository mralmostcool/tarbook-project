package com.mralmostcool.tarbook.program;

import com.mralmostcool.tarbook.program.dto.EligibilityEvaluationResultDto;
import com.mralmostcool.tarbook.program.dto.ProgramSyllabusDto;
import com.mralmostcool.tarbook.program.internal.domain.StcwProgram;
import com.mralmostcool.tarbook.program.internal.service.EligibilityRuleEngine;
import com.mralmostcool.tarbook.program.internal.service.ProgramSyllabusInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramSyllabusInternalService syllabusInternalService;
    private final EligibilityRuleEngine eligibilityRuleEngine;

    @Transactional(readOnly = true)
    public List<ProgramSyllabusDto> getAllPrograms() {
        return syllabusInternalService.findAllPrograms().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProgramSyllabusDto> getProgramById(UUID programId) {
        return syllabusInternalService.findProgramById(programId).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Optional<ProgramSyllabusDto> getProgramByCode(String code) {
        return syllabusInternalService.findProgramByCode(code).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public EligibilityEvaluationResultDto evaluateCadetEligibility(UUID programId, int seaDays, int watchkeepingHours) {
        StcwProgram program = syllabusInternalService.findProgramById(programId)
                .orElseThrow(() -> new IllegalArgumentException("STCW Program not found: " + programId));

        boolean isEligible = eligibilityRuleEngine.isEligibleForAssessment(programId, seaDays, watchkeepingHours);

        return EligibilityEvaluationResultDto.builder()
                .programId(programId)
                .eligible(isEligible)
                .actualSeaDays(seaDays)
                .requiredSeaDays(program.getTotalRequiredSeaDays())
                .actualWatchkeepingHours(watchkeepingHours)
                .statusMessage(isEligible ? "Cadet meets STCW sea-time requirements" : "Cadet sea-time criteria not met")
                .build();
    }

    private ProgramSyllabusDto mapToDto(StcwProgram program) {
        return ProgramSyllabusDto.builder()
                .id(program.getId())
                .code(program.getCode())
                .title(program.getTitle())
                .stream(program.getStream())
                .totalRequiredSeaDays(program.getTotalRequiredSeaDays())
                .createdAtUtc(program.getCreatedAtUtc())
                .build();
    }
}
