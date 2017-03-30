/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that at least 1 ChannelConfig or 1 RegisterConfig is defined on an active configuration for:
 * <ul>
 * <li>A datalogger enabled DeviceConfiguration</li>
 * <li>Each config of a DataloggerSlave DeviceType</li>
 * </ul>
 */
public class DataSourcesValidator implements ConstraintValidator<ValidDataSources, DeviceConfigurationImpl> {
    @Override
    public void initialize(ValidDataSources constraintAnnotation) {

    }

    @Override
    public boolean isValid(DeviceConfigurationImpl deviceConfiguration, ConstraintValidatorContext context) {
        if (deviceConfiguration.isActive()) {
            if (noDataSources(deviceConfiguration) && deviceConfiguration.isDataloggerEnabled()) {
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DATALOGGER_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE + "}")
                        .addConstraintViolation().disableDefaultConstraintViolation();
                return false;
            } else if (noDataSources(deviceConfiguration) && deviceConfiguration.getDeviceType().isDataloggerSlave()) {
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DATALOGGER_SLAVES_AT_LEAST_ONE_DATASOURCE + "}")
                        .addConstraintViolation().disableDefaultConstraintViolation();
                return false;
            }
        }
        return true;
    }

    private boolean noDataSources(DeviceConfigurationImpl deviceConfiguration) {
        return deviceConfiguration.getChannelSpecs().isEmpty() && deviceConfiguration.getRegisterSpecs().isEmpty();
    }
}
