/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link ComTaskIsEnabledOnlyOncePerConfiguration} constraint against a {@link ComTaskEnablementImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (09:50)
 */
public class ComTaskIsEnabledOnlyOncePerConfigurationValidator implements ConstraintValidator<ComTaskIsEnabledOnlyOncePerConfiguration, ComTaskEnablementImpl> {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ComTaskIsEnabledOnlyOncePerConfigurationValidator(DeviceConfigurationService deviceConfigurationService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void initialize(ComTaskIsEnabledOnlyOncePerConfiguration constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskEnablementImpl comTaskEnablement, ConstraintValidatorContext context) {
        ComTask comTask = comTaskEnablement.getComTask();
        DeviceConfiguration deviceConfiguration = comTaskEnablement.getDeviceConfiguration();
        // Both ComTask and DeviceConfiguration are required but other validators are responsible for checking that
        if (comTask != null && deviceConfiguration != null) {
            Optional<ComTaskEnablement> otherComTaskEnablement = deviceConfiguration.getComTaskEnablementFor(comTask);
            // Validation is @ creation time only, so if another is found, it really must be another one
            if (otherComTaskEnablement.isPresent()) {
                context.disableDefaultConstraintViolation();
                context.
                    buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).
                    addPropertyNode(ComTaskEnablementImpl.Fields.COM_TASK.fieldName()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }

}