package com.elster.jupiter.estimation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface EstimationService {

    String COMPONENTNAME = "EST";

    /**
     * Filters estimators and returns names of implementations supporting a given <code>qualityCodeSystem</code>.
     * @param qualityCodeSystem a target QualityCodeSystem.
     * @return the list of estimator implementation names supporting a given <code>qualityCodeSystem</code>.
     */
    List<String> getAvailableEstimatorImplementations(QualityCodeSystem qualityCodeSystem);

    /**
     * Filters estimators and returns ones supporting a given <code>qualityCodeSystem</code>.
     * @param qualityCodeSystem a target QualityCodeSystem.
     * @return the list of estimators supporting a given <code>qualityCodeSystem</code>.
     */
    List<Estimator> getAvailableEstimators(QualityCodeSystem qualityCodeSystem);

    Optional<Estimator> getEstimator(String implementation);

    Optional<Estimator> getEstimator(String implementation, Map<String, Object> props);

    EstimationReport estimate(MeterActivation meterActivation, Range<Instant> period);

    EstimationReport previewEstimate(MeterActivation meterActivation, Range<Instant> period);

    EstimationReport previewEstimate(MeterActivation meterActivation, Range<Instant> period, ReadingType readingType);

    EstimationResult previewEstimate(MeterActivation meterActivation, Range<Instant> period, ReadingType readingType, Estimator estimator);

    EstimationRuleSet createEstimationRuleSet(String name, QualityCodeSystem qualityCodeSystem);

    EstimationRuleSet createEstimationRuleSet(String name, QualityCodeSystem qualityCodeSystem, String description);

    List<? extends EstimationRuleSet> getEstimationRuleSets();

    Optional<? extends EstimationRuleSet> getEstimationRuleSet(long id);

    Optional<? extends EstimationRuleSet> findAndLockEstimationRuleSet(long id, long version);

    Optional<? extends EstimationRuleSet> getEstimationRuleSet(String name);

    boolean isEstimationRuleSetInUse(EstimationRuleSet validationRuleSet);

    Optional<? extends EstimationRule> getEstimationRule(long id);

    Optional<? extends EstimationRule> findAndLockEstimationRule(long id, long version);

    EstimationTaskBuilder newBuilder();

    List<? extends EstimationTask> findEstimationTasks(String application);

    Optional<? extends EstimationTask> findEstimationTask(long id);

    Optional<? extends EstimationTask> findAndLockEstimationTask(long id, long version);

    Query<EstimationRuleSet> getEstimationRuleSetQuery();

    Query<? extends EstimationTask> getEstimationTaskQuery();

    Optional<? extends  EstimationRule> findEstimationRuleByQualityType(ReadingQualityType readingQualityType);
}
