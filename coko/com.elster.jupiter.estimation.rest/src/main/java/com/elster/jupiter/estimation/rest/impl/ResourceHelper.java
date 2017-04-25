/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;
import java.util.Optional;

class ResourceHelper {

    private final EstimationService estimationService;

    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;

    @Inject
    ResourceHelper(EstimationService estimationService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory) {
        this.estimationService = estimationService;
        this.exceptionFactory = exceptionFactory;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
    }

    Estimator findEstimatorOrThrowException(String implementation) {
        return estimationService.getEstimator(implementation)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_ESTIMATOR, implementation));
    }

    EstimationRuleSet findEstimationRuleSetOrThrowException(long ruleSetId) {
        return estimationService.getEstimationRuleSet(ruleSetId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_ESTIMATION_RULESET, ruleSetId));
    }

    EstimationRuleSet findAndLockEstimationRuleSet(EstimationRuleSetInfo info) {
        return estimationService.findAndLockEstimationRuleSet(info.id, info.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> estimationService.getEstimationRuleSet(info.id)
                                .filter(candidate -> candidate.getObsoleteDate() == null)
                                .map(EstimationRuleSet::getVersion)
                                .orElse(null))
                        .supplier());
    }

    EstimationRule findEstimationRuleInRuleSetOrThrowException(EstimationRuleSet ruleSet, long ruleId) {
        return ruleSet.getRules().stream()
                .filter(input -> input.getId() == ruleId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_ESTIMATION_RULE_IN_RULESET, ruleId, ruleSet.getName()));
    }

    EstimationRule findAndLockRule(EstimationRuleInfo info) {
        Optional<? extends EstimationRuleSet> ruleSet = estimationService.findAndLockEstimationRuleSet(info.parent.id, info.parent.version);
        Long actualRuleVersion = null;
        Long actualParentVersion = null;
        if (!ruleSet.isPresent()) { // parent was changed or deleted
            Optional<? extends EstimationRuleSet> unlockedRuleSet = estimationService.getEstimationRuleSet(info.parent.id);
            // if rule set was deleted, the rule should be deleted as well, so both should have the 'null' version
            if (unlockedRuleSet.isPresent() && unlockedRuleSet.get().getObsoleteDate() == null) {
                actualParentVersion = unlockedRuleSet.get().getVersion();
                actualRuleVersion = getCurrentEstimationRuleVersion(info, unlockedRuleSet);
            }
        } else { // no changes in parent
            actualParentVersion = ruleSet.get().getVersion();
            Optional<? extends EstimationRule> estimationRule = estimationService.findAndLockEstimationRule(info.id, info.version);
            if (!estimationRule.isPresent()) { // but rule itself was changed
                actualRuleVersion = getCurrentEstimationRuleVersion(info, ruleSet);
            } else { // no changes in rule
                return estimationRule.get();
            }
        }
        Long ruleVersion = actualRuleVersion;
        Long parentVersion = actualParentVersion;
        throw concurrentModificationExceptionFactory.contextDependentConflictOn(info.name)
                .withActualVersion(() -> ruleVersion)
                .withActualParent(() -> parentVersion, info.parent.id)
                .build();
    }

    private Long getCurrentEstimationRuleVersion(EstimationRuleInfo info, Optional<? extends EstimationRuleSet> ruleSet) {
        return ruleSet.get().getRules().stream()
                .filter(input -> input.getId() == info.id && !input.isObsolete())
                .findAny()
                .map(EstimationRule::getVersion)
                .orElse(null);
    }
}
