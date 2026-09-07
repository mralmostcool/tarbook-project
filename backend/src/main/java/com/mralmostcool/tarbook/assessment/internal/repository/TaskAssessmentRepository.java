package com.mralmostcool.tarbook.assessment.internal.repository;

import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentStatus;
import com.mralmostcool.tarbook.assessment.internal.domain.TaskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAssessmentRepository extends JpaRepository<TaskAssessment, UUID> {

    List<TaskAssessment> findByCandidateUserId(UUID candidateUserId);

    List<TaskAssessment> findByTarBookId(UUID tarBookId);

    Optional<TaskAssessment> findByTarBookIdAndTaskDefinitionId(UUID tarBookId, UUID taskDefinitionId);

    long countByTarBookId(UUID tarBookId);

    long countByTarBookIdAndStatus(UUID tarBookId, AssessmentStatus status);

    long countByTarBookIdAndTaskDefinitionFunctionId(UUID tarBookId, UUID functionId);

    long countByTarBookIdAndTaskDefinitionFunctionIdAndStatus(UUID tarBookId, UUID functionId, AssessmentStatus status);
}
