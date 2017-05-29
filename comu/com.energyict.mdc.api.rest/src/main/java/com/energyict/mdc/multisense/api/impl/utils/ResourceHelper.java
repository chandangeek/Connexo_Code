/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl.utils;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by bvn on 7/15/15.
 */
public class ResourceHelper {

    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final Validator validator;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ResourceHelper(DeviceService deviceService, DeviceConfigurationService deviceConfigurationService, ExceptionFactory exceptionFactory, Validator validator) {
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.exceptionFactory = exceptionFactory;
        this.validator = validator;
    }

    public Device findDeviceByMrIdOrThrowException(String mrid) {
        return deviceService
                .findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
    }

    public void validate(Object info, Class<?> clazz) {
        Set<ConstraintViolation<Object>> violations = validator.validate(info, clazz);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

    }

    public List<DeviceConfiguration> findDeviceConfigurationsApplicableToMetrologyConfig(DeviceType deviceType, UsagePoint usagePoint, MeterRole meterRole, Instant shipmentDate) {
        Range<Instant> shipmentRange = Range.atLeast(shipmentDate);
        List<DeviceConfiguration> applicableConfigurations = new ArrayList<>();
        usagePoint.getEffectiveMetrologyConfigurations().stream()
                .filter(emc -> emc.getRange().isConnected(shipmentRange) && !emc.getRange().intersection(shipmentRange).isEmpty())
                .forEach(effectiveMetrologyConfiguration -> {
                    Set<ReadingTypeRequirement> requirements = effectiveMetrologyConfiguration.getReadingTypeRequirements().stream()
                            .filter(readingTypeRequirement -> meterRole.equals(effectiveMetrologyConfiguration.getMetrologyConfiguration()
                                    .getMeterRoleFor(readingTypeRequirement)
                                    .orElse(null)))
                            .collect(Collectors.toSet());
                    deviceType.getConfigurations().stream()
                            .filter(DeviceConfiguration::isActive)
                            .forEach(deviceConfiguration -> {
                                        List<ReadingType> providedReadingTypes = deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration);
                                        if (requirements.stream().noneMatch(requirement -> !providedReadingTypes.stream().anyMatch(requirement::matches))) {
                                            applicableConfigurations.add(deviceConfiguration);
                                        }
                                    }
                            );
                });
        return applicableConfigurations;
    }
}
