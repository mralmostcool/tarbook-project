package com.mralmostcool.tarbook.program;

import com.mralmostcool.tarbook.core.internal.domain.TrainingStream;
import com.mralmostcool.tarbook.program.dto.EligibilityEvaluationResultDto;
import com.mralmostcool.tarbook.program.dto.ProgramSyllabusDto;
import com.mralmostcool.tarbook.program.internal.domain.CadetEligibilityRule;
import com.mralmostcool.tarbook.program.internal.domain.StcwProgram;
import com.mralmostcool.tarbook.program.internal.service.EligibilityRuleEngine;
import com.mralmostcool.tarbook.program.internal.service.ProgramSyllabusInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramServiceUnitTest {

    private ProgramService programService;
    private List<StcwProgram> programStore;
    private List<CadetEligibilityRule> ruleStore;

    private UUID programId;
    private StcwProgram program;

    @BeforeEach
    void setUp() {
        programId = UUID.randomUUID();
        programStore = new ArrayList<>();
        ruleStore = new ArrayList<>();

        program = StcwProgram.builder()
                .id(programId)
                .code("DNS-STCW-01")
                .title("Diploma in Nautical Science")
                .stream(TrainingStream.DECK_CADET)
                .totalRequiredSeaDays(365)
                .createdAtUtc(OffsetDateTime.now())
                .build();
        programStore.add(program);

        CadetEligibilityRule rule = CadetEligibilityRule.builder()
                .id(UUID.randomUUID())
                .program(program)
                .ruleCode("RULE-DNS-365")
                .minSeaDays(365)
                .minBridgeWatchkeepingHours(500)
                .build();
        ruleStore.add(rule);

        ProgramSyllabusInternalService syllabusInternalService = new ProgramSyllabusInternalService(null, null, null) {
            @Override
            public List<StcwProgram> findAllPrograms() {
                return programStore;
            }

            @Override
            public Optional<StcwProgram> findProgramById(UUID id) {
                return programStore.stream().filter(p -> p.getId().equals(id)).findFirst();
            }

            @Override
            public Optional<StcwProgram> findProgramByCode(String code) {
                return programStore.stream().filter(p -> p.getCode().equals(code)).findFirst();
            }
        };

        EligibilityRuleEngine eligibilityRuleEngine = new EligibilityRuleEngine(null) {
            @Override
            public boolean isEligibleForAssessment(UUID pId, int actualSeaDays, int actualWatchkeepingHours) {
                Optional<CadetEligibilityRule> rOpt = ruleStore.stream().filter(r -> r.getProgram().getId().equals(pId)).findFirst();
                if (rOpt.isEmpty()) return false;
                CadetEligibilityRule r = rOpt.get();
                return actualSeaDays >= r.getMinSeaDays() && actualWatchkeepingHours >= r.getMinBridgeWatchkeepingHours();
            }
        };

        programService = new ProgramService(syllabusInternalService, eligibilityRuleEngine);
    }

    @Test
    void shouldGetAllPrograms() {
        List<ProgramSyllabusDto> programs = programService.getAllPrograms();
        assertThat(programs).hasSize(1);
        assertThat(programs.get(0).getCode()).isEqualTo("DNS-STCW-01");
    }

    @Test
    void shouldGetProgramById() {
        Optional<ProgramSyllabusDto> dtoOpt = programService.getProgramById(programId);
        assertThat(dtoOpt).isPresent();
        assertThat(dtoOpt.get().getTitle()).isEqualTo("Diploma in Nautical Science");
    }

    @Test
    void shouldEvaluateCadetAsEligibleWhenCriteriaMet() {
        EligibilityEvaluationResultDto result = programService.evaluateCadetEligibility(programId, 370, 600);
        assertThat(result.isEligible()).isTrue();
        assertThat(result.getStatusMessage()).contains("meets STCW sea-time");
    }

    @Test
    void shouldEvaluateCadetAsIneligibleWhenSeaDaysInsufficient() {
        EligibilityEvaluationResultDto result = programService.evaluateCadetEligibility(programId, 200, 600);
        assertThat(result.isEligible()).isFalse();
        assertThat(result.getStatusMessage()).contains("not met");
    }
}
