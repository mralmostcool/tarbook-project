package com.mralmostcool.tarbook.program.internal.service;

import com.mralmostcool.tarbook.program.internal.domain.StcwProgram;
import com.mralmostcool.tarbook.program.internal.domain.SyllabusFunction;
import com.mralmostcool.tarbook.program.internal.domain.SyllabusTask;
import com.mralmostcool.tarbook.program.internal.repository.StcwProgramRepository;
import com.mralmostcool.tarbook.program.internal.repository.SyllabusFunctionRepository;
import com.mralmostcool.tarbook.program.internal.repository.SyllabusTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgramSyllabusInternalService {

    private final StcwProgramRepository programRepository;
    private final SyllabusFunctionRepository functionRepository;
    private final SyllabusTaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<StcwProgram> findAllPrograms() {
        return programRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<StcwProgram> findProgramById(UUID programId) {
        return programRepository.findById(programId);
    }

    @Transactional(readOnly = true)
    public Optional<StcwProgram> findProgramByCode(String code) {
        return programRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<SyllabusFunction> findFunctionsByProgramId(UUID programId) {
        return functionRepository.findByProgramIdOrderByDisplayOrderAsc(programId);
    }

    @Transactional(readOnly = true)
    public List<SyllabusTask> findTasksByFunctionId(UUID functionId) {
        return taskRepository.findByFunctionId(functionId);
    }

    @Transactional
    public StcwProgram saveProgram(StcwProgram program) {
        return programRepository.save(program);
    }

    @Transactional
    public SyllabusFunction saveFunction(SyllabusFunction function) {
        return functionRepository.save(function);
    }

    @Transactional
    public SyllabusTask saveTask(SyllabusTask task) {
        return taskRepository.save(task);
    }
}
