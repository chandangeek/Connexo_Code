/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.elster.jupiter.util.Checks.is;

/**
 * Validates the {@link ProtocolDialectConfigurationPropertiesMustBeFromSameConfiguration} constraint against a {@link ComTaskEnablementImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (11:36)
 */
public class ProtocolDialectConfigurationPropertiesMustBeFromSameConfigurationValidator implements ConstraintValidator<ProtocolDialectConfigurationPropertiesMustBeFromSameConfiguration, PartialConnectionTaskImpl> {

    @Override
    public void initialize(ProtocolDialectConfigurationPropertiesMustBeFromSameConfiguration constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(PartialConnectionTaskImpl partialConnectionTask, ConstraintValidatorContext context) {
        ProtocolDialectConfigurationProperties dialectConfigurationProperties = partialConnectionTask.getProtocolDialectConfigurationProperties();
        if (dialectConfigurationProperties != null && this.notSameConfiguration(dialectConfigurationProperties, partialConnectionTask)) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(PartialConnectionTaskImpl.Fields.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES.fieldName()).addConstraintViolation();
        }
        return true;
    }

    private boolean notSameConfiguration(ProtocolDialectConfigurationProperties dialectConfigurationProperties, PartialConnectionTaskImpl partialConnectionTask) {
        long dialectConfigurationId = dialectConfigurationProperties.getDeviceConfiguration().getId();
        long configurationId = partialConnectionTask.getConfiguration().getId();
        return !is(configurationId).equalTo(dialectConfigurationId);
    }

}