package com.elster.jupiter.estimation;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EstimationService {

    String COMPONENTNAME = "EST";

    List<String> getAvailableEstimatorImplementations();

    List<Estimator> getAvailableEstimators();

    Optional<Estimator> getEstimator(String implementation);

    Optional<Estimator> getEstimator(String implementation, Map<String, Object> props);

    EstimationReport estimate(MeterActivation meterActivation, Range<Instant> period);

    EstimationReport previewEstimate(MeterActivation meterActivation, Range<Instant> period);

    EstimationReport previewEstimate(MeterActivation meterActivation, Range<Instant> period, ReadingType readingType);

    EstimationResult previewEstimate(MeterActivation meterActivation, Range<Instant> period, ReadingType readingType, Estimator estimator);

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

    Optional<? extends  EstimationRule> findEstimationRuleByQualityType(ReadingQualityType readingQualityType);
}
