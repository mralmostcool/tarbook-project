package com.mralmostcool.tarbook.program.internal.service;

import com.mralmostcool.tarbook.program.internal.domain.CadetEligibilityRule;
import com.mralmostcool.tarbook.program.internal.repository.CadetEligibilityRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EligibilityRuleEngine {

    private final CadetEligibilityRuleRepository eligibilityRuleRepository;

    @Transactional(readOnly = true)
    public boolean isEligibleForAssessment(UUID programId, int actualSeaDays, int actualWatchkeepingHours) {
        Optional<CadetEligibilityRule> ruleOpt = eligibilityRuleRepository.findByProgramId(programId);
        if (ruleOpt.isEmpty()) {
            return false;
        }
        CadetEligibilityRule rule = ruleOpt.get();

        if (actualSeaDays < rule.getMinSeaDays()) {
            return false;
        }

        if (rule.getMinBridgeWatchkeepingHours() != null && actualWatchkeepingHours < rule.getMinBridgeWatchkeepingHours()) {
            return false;
        }

        if (rule.getMinEngineWatchkeepingHours() != null && actualWatchkeepingHours < rule.getMinEngineWatchkeepingHours()) {
            return false;
        }

        return true;
    }

    @Transactional
    public CadetEligibilityRule saveRule(CadetEligibilityRule rule) {
        return eligibilityRuleRepository.save(rule);
    }
}
