/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.config.DeviceType;

import com.google.common.collect.Range;

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
    private ValidationService validationService;
    @Mock
    private EstimationService estimationService;
    @Mock
    private EndDeviceGroup endDeviceGroup1, endDeviceGroup2;
    @Mock
    private DeviceType deviceType1, deviceType2;
    @Mock
    private Validator validator1, validator2;
    @Mock
    private Estimator estimator1, estimator2;

    private DeviceDataQualityServiceImpl deviceDataQualityService;

    @Before
    public void setUp() {
        when(ormService.getDataModel(any())).thenReturn(Optional.of(dataModel));
        deviceDataQualityService = new DeviceDataQualityServiceImpl(ormService, validationService, estimationService);
    }

    @Test
    public void buildWithDeviceGroups() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .in(Collections.emptyList())
                .in(Collections.singletonList(endDeviceGroup1))
                .in(Arrays.asList(endDeviceGroup2, endDeviceGroup1));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getDeviceGroups()).containsOnly(endDeviceGroup1, endDeviceGroup2);
    }

    @Test
    public void buildWithDeviceTypes() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .of(Collections.emptyList())
                .of(Collections.singletonList(deviceType1))
                .of(Arrays.asList(deviceType2, deviceType1));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getDeviceTypes()).containsOnly(deviceType1, deviceType2);
    }

    @Test
    public void buildWithTimePeriod() {
        Instant now = Instant.now();
        Range<Instant> period = Range.open(now.minusSeconds(1), now.plusSeconds(1));

        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .in(period);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getPeriod()).isEqualTo(period);
    }

    @Test
    public void buildForSuspects() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .havingSuspects();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(KpiType.SUSPECT);
    }

    @Test
    public void buildForEstimates() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .havingEstimates();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(KpiType.ESTIMATED);
    }

    @Test
    public void buildForEdited() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .havingEdited();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEdited()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfEdited()).getRange()).isEqualTo(Range.atLeast(1L));
    }

    @Test
    public void buildForConfirmed() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .havingConfirmed();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(KpiType.CONFIRMED);
    }

    @Test
    public void buildForInformatives() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .havingInformatives();

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(KpiType.INFORMATIVE);
    }

    @Test
    public void buildWithExactMatchAmountOfSuspects() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .suspects()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfSuspects()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfSuspects()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfSuspects() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .suspects()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfSuspects()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfSuspects()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithExactMatchAmountOfEstimates() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .estimates()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEstimates()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfEstimates()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfEstimates() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .estimates()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEstimates()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfEstimates()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithExactMatchAmountOfEdited() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .edited()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEdited()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfEdited()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfEdited() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .edited()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfEdited()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfEdited()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithExactMatchAmountOfConfirmed() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .confirmed()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfConfirmed()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfConfirmed()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfConfirmed() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .confirmed()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfConfirmed()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfConfirmed()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithExactMatchAmountOfInformatives() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .informatives()
                .equalTo(10);

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfInformatives()).isInstanceOf(MetricValueRange.ExactMatch.class);
        assertThat(((MetricValueRange.ExactMatch) spec.getAmountOfInformatives()).getMatch()).isEqualTo(10);
    }

    @Test
    public void buildWithRangeMatchAmountOfInformatives() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .informatives()
                .inRange(Range.atLeast(10L));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getAmountOfInformatives()).isInstanceOf(MetricValueRange.LongRange.class);
        assertThat(((MetricValueRange.LongRange) spec.getAmountOfInformatives()).getRange()).isEqualTo(Range.atLeast(10L));
    }

    @Test
    public void buildWithValidators() {
        // Business method
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
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
        DataQualityOverviewBuilderImpl builder = (DataQualityOverviewBuilderImpl) deviceDataQualityService
                .forAllDevices()
                .estimatedBy(Collections.emptyList())
                .estimatedBy(Collections.singletonList(estimator1))
                .estimatedBy(Arrays.asList(estimator2, estimator1));

        // Asserts
        DataQualityOverviewSpecificationImpl spec = builder.getSpecification();
        assertThat(spec.getEnabledKpiTypes()).containsOnly(new KpiType.EstimatorKpiType(estimator1), new KpiType.EstimatorKpiType(estimator2));
    }
}
