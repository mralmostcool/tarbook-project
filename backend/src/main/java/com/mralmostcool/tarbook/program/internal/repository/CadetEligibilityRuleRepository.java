package com.mralmostcool.tarbook.program.internal.repository;

import com.mralmostcool.tarbook.program.internal.domain.CadetEligibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CadetEligibilityRuleRepository extends JpaRepository<CadetEligibilityRule, UUID> {
    Optional<CadetEligibilityRule> findByProgramId(UUID programId);
}
