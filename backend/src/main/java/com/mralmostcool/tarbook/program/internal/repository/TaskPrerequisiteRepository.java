package com.mralmostcool.tarbook.program.internal.repository;

import com.mralmostcool.tarbook.program.internal.domain.TaskPrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskPrerequisiteRepository extends JpaRepository<TaskPrerequisite, UUID> {
    List<TaskPrerequisite> findByTaskId(UUID taskId);
}
