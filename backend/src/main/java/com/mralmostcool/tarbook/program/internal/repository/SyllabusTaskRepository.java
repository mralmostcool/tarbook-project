package com.mralmostcool.tarbook.program.internal.repository;

import com.mralmostcool.tarbook.program.internal.domain.SyllabusTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SyllabusTaskRepository extends JpaRepository<SyllabusTask, UUID> {
    List<SyllabusTask> findByFunctionId(UUID functionId);
}
