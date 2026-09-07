package com.mralmostcool.tarbook.assessment.internal.repository;

import com.mralmostcool.tarbook.assessment.internal.domain.AssessmentSignOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentSignOffRepository extends JpaRepository<AssessmentSignOff, UUID> {

    List<AssessmentSignOff> findByTaskAssessmentIdOrderBySignedAtUtcAsc(UUID taskAssessmentId);
}
