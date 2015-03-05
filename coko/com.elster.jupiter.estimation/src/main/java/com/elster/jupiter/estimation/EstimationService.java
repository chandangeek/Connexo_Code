package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;
import java.util.Optional;

public interface EstimationService {

    String COMPONENTNAME = "EST";

    List<Estimator> getAvailableEstimators();

    Optional<Estimator> getEstimator(String implementation);

    void estimate(MeterActivation meterActivation);

    void estimate(MeterActivation meterActivation, ReadingType readingType);

    void estimate(MeterActivation meterActivation, ReadingType readingType, Estimator estimator);

    EstimationRuleSet createEstimationRuleSet(String name);

    List<? extends EstimationRuleSet> getEstimationRuleSets();

    Optional<? extends EstimationRuleSet> getEstimationRuleSet(long id);

    Optional<? extends EstimationRuleSet> getEstimationRuleSet(String name);

    boolean isEstimationRuleSetInUse(EstimationRuleSet validationRuleSet);

    Optional<? extends EstimationRule> getEstimationRule(long id);

}
