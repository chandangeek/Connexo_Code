/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;

public class ResourceHelper {

    private final MeteringGroupsService meteringGroupsService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ValidationService validationService;
    private final EstimationService estimationService;

    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(MeteringGroupsService meteringGroupsService, DeviceConfigurationService deviceConfigurationService,
                          ValidationService validationService, EstimationService estimationService, ExceptionFactory exceptionFactory) {
        this.meteringGroupsService = meteringGroupsService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.exceptionFactory = exceptionFactory;
    }

    public EndDeviceGroup findEndDeviceGroupOrThrowException(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId)
                .orElseThrow(() -> newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, endDeviceGroupId));
    }

    public DeviceType findDeviceTypeOrThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(() -> newException(MessageSeeds.NO_SUCH_DEVICE_TYPE, deviceTypeId));
    }

    public Validator findValidatorOrThrowException(String implementation) {
        return validationService.getAvailableValidators(QualityCodeSystem.MDC).stream()
                .filter(validator -> validator.getClass().getName().equals(implementation))
                .findAny()
                .orElseThrow(() -> newException(MessageSeeds.NO_SUCH_VALIDATOR, implementation));
    }

    public Estimator findEstimatorOrThrowException(String implementation) {
        return estimationService.getAvailableEstimators(QualityCodeSystem.MDC).stream()
                .filter(estimator -> estimator.getClass().getName().equals(implementation))
                .findAny()
                .orElseThrow(() -> newException(MessageSeeds.NO_SUCH_ESTIMATOR, implementation));
    }

    public LocalizedException newException(MessageSeeds messageSeed, Object... args) {
        return exceptionFactory.newException(messageSeed, args);
    }
}
