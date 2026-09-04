package com.mralmostcool.tarbook.program.internal.service;

import com.mralmostcool.tarbook.program.internal.domain.TaskPrerequisite;
import com.mralmostcool.tarbook.program.internal.repository.TaskPrerequisiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskPrerequisiteEvaluator {

    private final TaskPrerequisiteRepository prerequisiteRepository;

    @Transactional(readOnly = true)
    public boolean isTaskUnlocked(UUID taskId, Set<UUID> completedTaskIds) {
        List<TaskPrerequisite> prerequisites = prerequisiteRepository.findByTaskId(taskId);
        if (prerequisites.isEmpty()) {
            return true;
        }
        if (completedTaskIds == null || completedTaskIds.isEmpty()) {
            return false;
        }
        return prerequisites.stream()
                .allMatch(prereq -> completedTaskIds.contains(prereq.getPrerequisiteTask().getId()));
    }
}
