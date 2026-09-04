package com.mralmostcool.tarbook.program.internal.repository;

import com.mralmostcool.tarbook.program.internal.domain.SyllabusFunction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SyllabusFunctionRepository extends JpaRepository<SyllabusFunction, UUID> {
    List<SyllabusFunction> findByProgramIdOrderByDisplayOrderAsc(UUID programId);
}
