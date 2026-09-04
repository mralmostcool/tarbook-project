package com.mralmostcool.tarbook.program;

import com.mralmostcool.tarbook.program.dto.EligibilityEvaluationResultDto;
import com.mralmostcool.tarbook.program.dto.ProgramSyllabusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
@Tag(name = "STCW Programs & Syllabus", description = "Endpoints for maritime training syllabus and eligibility evaluation")
public class ProgramController {

    private final ProgramService programService;

    @GetMapping
    @Operation(summary = "List all registered STCW programs")
    public ResponseEntity<List<ProgramSyllabusDto>> getAllPrograms() {
        return ResponseEntity.ok(programService.getAllPrograms());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get STCW program details by ID")
    public ResponseEntity<ProgramSyllabusDto> getProgramById(@PathVariable UUID id) {
        return programService.getProgramById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/eligibility-check")
    @Operation(summary = "Evaluate cadet STCW sea-time eligibility")
    public ResponseEntity<EligibilityEvaluationResultDto> checkEligibility(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int seaDays,
            @RequestParam(defaultValue = "0") int watchkeepingHours) {
        return ResponseEntity.ok(programService.evaluateCadetEligibility(id, seaDays, watchkeepingHours));
    }
}
