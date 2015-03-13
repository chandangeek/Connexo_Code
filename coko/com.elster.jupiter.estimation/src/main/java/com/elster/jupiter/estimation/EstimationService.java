package com.elster.jupiter.estimation;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;
import java.util.Optional;

public interface EstimationService {

    String COMPONENTNAME = "EST";

    List<Estimator> getAvailableEstimators();

    Optional<Estimator> getEstimator(String implementation);

    EstimationReport estimate(MeterActivation meterActivation);

    EstimationReport estimate(MeterActivation meterActivation, ReadingType readingType);

    void estimate(MeterActivation meterActivation, ReadingType readingType, Estimator estimator);

    EstimationRuleSet createEstimationRuleSet(String name);

    EstimationRuleSet createEstimationRuleSet(String name, String description);

    List<? extends EstimationRuleSet> getEstimationRuleSets();

    Optional<? extends EstimationRuleSet> getEstimationRuleSet(long id);

    Optional<? extends EstimationRuleSet> getEstimationRuleSet(String name);

    boolean isEstimationRuleSetInUse(EstimationRuleSet validationRuleSet);

    Optional<? extends EstimationRule> getEstimationRule(long id);

    EstimationTaskBuilder newBuilder();

    Optional<? extends EstimationTask> findEstimationTask(long id);

    Query<EstimationRuleSet> getEstimationRuleSetQuery();

    Query<? extends EstimationTask> getEstimationTaskQuery();
}
