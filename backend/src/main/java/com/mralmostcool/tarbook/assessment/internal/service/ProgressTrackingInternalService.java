package com.mralmostcool.tarbook.assessment.internal.service;

import com.mralmostcool.tarbook.assessment.dto.FunctionProgressDto;
import com.mralmostcool.tarbook.assessment.dto.ProgressReportDto;
import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentStatus;
import com.mralmostcool.tarbook.assessment.internal.repository.TaskAssessmentRepository;
import com.mralmostcool.tarbook.program.internal.domain.SyllabusFunction;
import com.mralmostcool.tarbook.program.internal.service.ProgramSyllabusInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressTrackingInternalService {

    private final TaskAssessmentRepository taskAssessmentRepository;
    private final ProgramSyllabusInternalService programSyllabusInternalService;

    @Transactional(readOnly = true)
    public ProgressReportDto calculateProgress(UUID tarBookId, UUID programId) {
        long totalTasks = taskAssessmentRepository.countByTarBookId(tarBookId);
        long completedTasks = taskAssessmentRepository.countByTarBookIdAndStatus(tarBookId, AssessmentStatus.APPROVED);

        double overallPct = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100.0 : 0.0;

        List<FunctionProgressDto> functionBreakdown = new ArrayList<>();
        if (programId != null) {
            List<SyllabusFunction> functions = programSyllabusInternalService.findFunctionsByProgramId(programId);
            for (SyllabusFunction fn : functions) {
                long fnTotal = taskAssessmentRepository.countByTarBookIdAndTaskDefinitionFunctionId(tarBookId, fn.getId());
                long fnCompleted = taskAssessmentRepository.countByTarBookIdAndTaskDefinitionFunctionIdAndStatus(tarBookId, fn.getId(), AssessmentStatus.APPROVED);
                double fnPct = fnTotal > 0 ? ((double) fnCompleted / fnTotal) * 100.0 : 0.0;

                functionBreakdown.add(FunctionProgressDto.builder()
                        .functionId(fn.getId())
                        .functionCode(fn.getFunctionCode())
                        .functionTitle(fn.getTitle())
                        .totalTasks(fnTotal)
                        .completedTasks(fnCompleted)
                        .completionPercentage(Math.round(fnPct * 100.0) / 100.0)
                        .build());
            }
        }

        return ProgressReportDto.builder()
                .tarBookId(tarBookId)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .overallCompletionPercentage(Math.round(overallPct * 100.0) / 100.0)
                .functionBreakdown(functionBreakdown)
                .build();
    }
}
