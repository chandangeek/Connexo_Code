/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.dataquality.DataQualityOverviews;
import com.energyict.mdc.device.dataquality.DeviceDataQualityService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DataQualityOverviewBuilderImpl implements DeviceDataQualityService.DataQualityOverviewBuilder {

    private final DeviceDataQualityServiceImpl deviceDataQualityService;
    private final ValidationService validationService;
    private final EstimationService estimationService;

    private final DataQualityOverviewSpecificationImpl specification;

    DataQualityOverviewBuilderImpl(DeviceDataQualityServiceImpl deviceDataQualityService, ValidationService validationService, EstimationService estimationService) {
        super();
        this.deviceDataQualityService = deviceDataQualityService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.specification = new DataQualityOverviewSpecificationImpl(availableKpiTypes());
    }

    List<KpiType> availableKpiTypes() {
        Stream<KpiType> validatorKpiTypes = validationService.getAvailableValidators(QualityCodeSystem.MDC).stream().map(KpiType.ValidatorKpiType::new);
        Stream<KpiType.EstimatorKpiType> estimatorKpiTypes = estimationService.getAvailableEstimators(QualityCodeSystem.MDC).stream().map(KpiType.EstimatorKpiType::new);

        return Stream.of(KpiType.predefinedKpiTypes.stream(), validatorKpiTypes, estimatorKpiTypes)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder in(Collection<EndDeviceGroup> deviceGroups) {
        this.specification.addDeviceGroups(deviceGroups);
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder of(Collection<DeviceType> deviceTypes) {
        this.specification.addDeviceTypes(deviceTypes);
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder in(Range<Instant> range) {
        this.specification.setPeriod(range);
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder havingSuspects() {
        this.specification.addKpiType(KpiType.SUSPECT, KpiType.CHANNEL, KpiType.REGISTER);
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder suspectedBy(Collection<Validator> validators) {
        this.specification.addKpiType(validators.stream().map(KpiType.ValidatorKpiType::new).toArray(KpiType[]::new));
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder havingConfirmed() {
        this.specification.addKpiType(KpiType.CONFIRMED);
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder havingEstimates() {
        this.specification.addKpiType(KpiType.ESTIMATED);
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder estimatedBy(Collection<Estimator> estimators) {
        this.specification.addKpiType(estimators.stream().map(KpiType.EstimatorKpiType::new).toArray(KpiType[]::new));
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder havingInformatives() {
        this.specification.addKpiType(KpiType.INFORMATIVE);
        return this;
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder havingEdited() {
        this.specification.addKpiType();
        return this;
    }

    @Override
    public DeviceDataQualityService.MetricSpecificationBuilder suspects() {
        return new DeviceDataQualityService.MetricSpecificationBuilder() {
            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfSuspects(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfSuspects(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public DeviceDataQualityService.MetricSpecificationBuilder confirmed() {
        return new DeviceDataQualityService.MetricSpecificationBuilder() {
            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfConfirmed(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfConfirmed(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public DeviceDataQualityService.MetricSpecificationBuilder estimates() {
        return new DeviceDataQualityService.MetricSpecificationBuilder() {
            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfEstimates(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfEstimates(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public DeviceDataQualityService.MetricSpecificationBuilder informatives() {
        return new DeviceDataQualityService.MetricSpecificationBuilder() {
            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfInformatives(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfInformatives(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public DeviceDataQualityService.MetricSpecificationBuilder edited() {
        return new DeviceDataQualityService.MetricSpecificationBuilder() {
            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfEdited(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DeviceDataQualityService.DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfEdited(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public DataQualityOverviews paged(int from, int to) {
        this.specification.paged(from, to);
        return deviceDataQualityService.queryWith(specification);
    }

    DataQualityOverviewSpecificationImpl getSpecification() {
        return specification;
    }
}