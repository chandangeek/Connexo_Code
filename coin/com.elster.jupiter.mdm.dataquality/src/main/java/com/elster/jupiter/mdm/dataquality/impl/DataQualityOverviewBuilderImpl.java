/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.mdm.dataquality.DataQualityOverviews;
import com.elster.jupiter.mdm.dataquality.UsagePointDataQualityService;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.mdm.dataquality.UsagePointDataQualityService.DataQualityOverviewBuilder;
import static com.elster.jupiter.mdm.dataquality.UsagePointDataQualityService.MetricSpecificationBuilder;

class DataQualityOverviewBuilderImpl implements UsagePointDataQualityService.DataQualityOverviewBuilder {

    private final UsagePointDataQualityServiceImpl deviceDataQualityService;
    private final ValidationService validationService;
    private final EstimationService estimationService;

    private final DataQualityOverviewSpecificationImpl specification;

    DataQualityOverviewBuilderImpl(UsagePointDataQualityServiceImpl deviceDataQualityService, ValidationService validationService, EstimationService estimationService) {
        super();
        this.deviceDataQualityService = deviceDataQualityService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.specification = new DataQualityOverviewSpecificationImpl(availableKpiTypes());
    }

    List<KpiType> availableKpiTypes() {
        Stream<KpiType> validatorKpiTypes = validationService.getAvailableValidators(QualityCodeSystem.MDM).stream().map(KpiType.ValidatorKpiType::new);
        Stream<KpiType> estimatorKpiTypes = estimationService.getAvailableEstimators(QualityCodeSystem.MDM).stream().map(KpiType.EstimatorKpiType::new);

        return Stream.of(KpiType.predefinedKpiTypes.stream(), validatorKpiTypes, estimatorKpiTypes)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public DataQualityOverviewBuilder in(Collection<UsagePointGroup> usagePointGroups) {
        this.specification.addUsagePointGroups(usagePointGroups);
        return this;
    }

    @Override
    public DataQualityOverviewBuilder of(Collection<MetrologyConfiguration> metrologyConfigurations) {
        this.specification.addMetrologyConfigurations(metrologyConfigurations);
        return this;
    }

    @Override
    public DataQualityOverviewBuilder with(Collection<MetrologyPurpose> purposes) {
        this.specification.addMetrologyPurposes(purposes);
        return this;
    }

    @Override
    public DataQualityOverviewBuilder in(Range<Instant> range) {
        this.specification.setPeriod(range);
        return this;
    }

    @Override
    public DataQualityOverviewBuilder havingSuspects() {
        this.specification.addKpiType(KpiType.SUSPECT);
        return this;
    }

    @Override
    public DataQualityOverviewBuilder suspectedBy(Collection<Validator> validators) {
        this.specification.addKpiType(validators.stream().map(KpiType.ValidatorKpiType::new).toArray(KpiType[]::new));
        return this;
    }

    @Override
    public DataQualityOverviewBuilder havingConfirmed() {
        this.specification.addKpiType(KpiType.CONFIRMED);
        return this;
    }

    @Override
    public DataQualityOverviewBuilder havingEstimates() {
        this.specification.addKpiType(KpiType.ESTIMATED);
        return this;
    }

    @Override
    public DataQualityOverviewBuilder estimatedBy(Collection<Estimator> estimators) {
        this.specification.addKpiType(estimators.stream().map(KpiType.EstimatorKpiType::new).toArray(KpiType[]::new));
        return this;
    }

    @Override
    public DataQualityOverviewBuilder havingInformatives() {
        this.specification.addKpiType(KpiType.INFORMATIVE);
        return this;
    }

    @Override
    public DataQualityOverviewBuilder havingEdited() {
        this.specification.setAmountOfEdited(Range.atLeast(1L));
        return this;
    }

    @Override
    public MetricSpecificationBuilder withSuspectsAmount() {
        return new MetricSpecificationBuilder() {
            @Override
            public DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfSuspects(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfSuspects(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public MetricSpecificationBuilder withConfirmedAmount() {
        return new MetricSpecificationBuilder() {
            @Override
            public DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfConfirmed(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfConfirmed(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public MetricSpecificationBuilder withEstimatesAmount() {
        return new MetricSpecificationBuilder() {
            @Override
            public DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfEstimates(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfEstimates(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public MetricSpecificationBuilder withInformativesAmount() {
        return new MetricSpecificationBuilder() {
            @Override
            public DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfInformatives(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DataQualityOverviewBuilder inRange(Range<Long> range) {
                specification.setAmountOfInformatives(range);
                return DataQualityOverviewBuilderImpl.this;
            }
        };
    }

    @Override
    public MetricSpecificationBuilder withEditedAmount() {
        return new MetricSpecificationBuilder() {
            @Override
            public DataQualityOverviewBuilder equalTo(long value) {
                specification.setAmountOfEdited(value);
                return DataQualityOverviewBuilderImpl.this;
            }

            @Override
            public DataQualityOverviewBuilder inRange(Range<Long> range) {
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