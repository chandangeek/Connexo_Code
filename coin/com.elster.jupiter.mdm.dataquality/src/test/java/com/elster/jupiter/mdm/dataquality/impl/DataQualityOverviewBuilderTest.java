/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataQualityOverviewBuilderTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private Clock clock;
    @Mock
    private ValidationService validationService;
    @Mock
    private EstimationService estimationService;
    @Mock
    private UsagePointGroup usagePointGroup1, usagePointGroup2;
    @Mock
    private MetrologyConfiguration metrologyConfiguration1, metrologyConfiguration2;
    @Mock
    private MetrologyPurpose metrologyPurpose1, metrologyPurpose2;
    @Mock
    private Validator validator1, validator2;
    @Mock
    private Estimator estimator1, estimator2;

    private UsagePointDataQualityServiceImpl usagePointDataQualityService;

    @Before
    public void setUp() {
        when(ormService.getDataModel(any())).thenReturn(Optional.of(dataModel));
        when(clock.instant()).thenReturn(Instant.now());
        usagePointDataQualityService = new UsagePointDataQualityServiceImpl(ormService, validationService, estimationService, clock);
    }

    @Test
    public void buildWithUsagePointGroups() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .in(Collections.emptyList())
                .in(Collections.singletonList(usagePointGroup1))
                .in(Arrays.asList(usagePointGroup2, usagePointGroup1));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getUsagePointGroups()).containsOnly(usagePointGroup1, usagePointGroup2);
    }

    @Test
    public void buildWithMetrologyConfigurations() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .of(Collections.emptyList())
                .of(Collections.singletonList(metrologyConfiguration1))
                .of(Arrays.asList(metrologyConfiguration1, metrologyConfiguration2));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getMetrologyConfigurations()).containsOnly(metrologyConfiguration1, metrologyConfiguration2);
    }

    @Test
    public void buildWithMetrologyPurposes() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .with(Collections.emptyList())
                .with(Collections.singletonList(metrologyPurpose1))
                .with(Arrays.asList(metrologyPurpose1, metrologyPurpose2));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getMetrologyPurposes()).containsOnly(metrologyPurpose1, metrologyPurpose2);
    }

    @Test
    public void buildWithTimePeriod() {
        Instant now = Instant.now();
        Range<Instant> period = Range.open(now.minusSeconds(1), now.plusSeconds(1));

        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .in(period);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getPeriod()).isEqualTo(period);
    }

    @Test
    public void buildForSuspects() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .havingSuspects();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(KpiType.SUSPECT);
    }

    @Test
    public void buildForEstimates() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .havingEstimates();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(KpiType.ESTIMATED);
    }

    @Test
    public void buildForEdited() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .havingEdited();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEdited()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfEdited()).getRange()).isEqualTo(Range.atLeast(1L));
    }

    @Test
    public void buildForConfirmed() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .havingConfirmed();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(KpiType.CONFIRMED);
    }

    @Test
    public void buildForInformatives() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .havingInformatives();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(KpiType.INFORMATIVE);
    }

    @Test
    public void buildWithExactMatchAmountOfSuspects() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withSuspectsAmount()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfSuspects()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfSuspects()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfSuspects() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withSuspectsAmount()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfSuspects()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfSuspects()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithExactMatchAmountOfEstimates() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withEstimatesAmount()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEstimates()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfEstimates()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfEstimates() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withEstimatesAmount()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEstimates()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfEstimates()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithExactMatchAmountOfEdited() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withEditedAmount()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEdited()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfEdited()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfEdited() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withEditedAmount()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEdited()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfEdited()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithExactMatchAmountOfConfirmed() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withConfirmedAmount()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfConfirmed()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfConfirmed()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfConfirmed() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withConfirmedAmount()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfConfirmed()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfConfirmed()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithExactMatchAmountOfInformatives() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withInformativesAmount()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfInformatives()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfInformatives()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfInformatives() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .withInformativesAmount()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfInformatives()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfInformatives()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithValidators() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .suspectedBy(Collections.emptyList())
                .suspectedBy(Collections.singletonList(validator1))
                .suspectedBy(Arrays.asList(validator2, validator1));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(new KpiType.ValidatorKpiType(validator1), new KpiType.ValidatorKpiType(validator2));
    }

    @Test
    public void buildWithEstimators() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) usagePointDataQualityService
                .forAllUsagePoints()
                .estimatedBy(Collections.emptyList())
                .estimatedBy(Collections.singletonList(estimator1))
                .estimatedBy(Arrays.asList(estimator2, estimator1));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(new KpiType.EstimatorKpiType(estimator1), new KpiType.EstimatorKpiType(estimator2));
    }
}
