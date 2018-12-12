/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import javax.inject.Inject;
import java.util.function.Supplier;

public class ResourceHelper {

    private final MeteringGroupsService meteringGroupsService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ValidationService validationService;
    private final EstimationService estimationService;

    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(MeteringGroupsService meteringGroupsService, MetrologyConfigurationService metrologyConfigurationService,
                          ValidationService validationService, EstimationService estimationService, ExceptionFactory exceptionFactory) {
        this.meteringGroupsService = meteringGroupsService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.exceptionFactory = exceptionFactory;
    }

    public UsagePointGroup findUsagePointGroupOrThrowException(long usagePointGroupId) {
        return meteringGroupsService.findUsagePointGroup(usagePointGroupId)
                .orElseThrow(newExceptionSupplier(MessageSeeds.NO_SUCH_USAGEPOINT_GROUP, usagePointGroupId));
    }

    public MetrologyConfiguration findMetrologyConfigurationOrThrowException(long metrologyConfigurationId) {
        return metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigurationId)
                .orElseThrow(newExceptionSupplier(MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION, metrologyConfigurationId));
    }

    public MetrologyPurpose findMetrologyPurposeOrThrowException(long metrologyPurposeId) {
        return metrologyConfigurationService.findMetrologyPurpose(metrologyPurposeId)
                .orElseThrow(newExceptionSupplier(MessageSeeds.NO_SUCH_METROLOGY_PURPOSE, metrologyPurposeId));
    }

    public Validator findValidatorOrThrowException(String implementation) {
        return validationService.getAvailableValidators(QualityCodeSystem.MDM).stream()
                .filter(validator -> validator.getClass().getName().equals(implementation))
                .findAny()
                .orElseThrow(newExceptionSupplier(MessageSeeds.NO_SUCH_VALIDATOR, implementation));
    }

    public Estimator findEstimatorOrThrowException(String implementation) {
        return estimationService.getAvailableEstimators(QualityCodeSystem.MDM).stream()
                .filter(estimator -> estimator.getClass().getName().equals(implementation))
                .findAny()
                .orElseThrow(newExceptionSupplier(MessageSeeds.NO_SUCH_ESTIMATOR, implementation));
    }

    public Supplier<LocalizedException> newExceptionSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> newException(messageSeed, args);
    }

    public LocalizedException newException(MessageSeeds messageSeed, Object... args) {
        return exceptionFactory.newException(messageSeed, args);
    }
}
