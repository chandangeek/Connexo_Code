/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.SecurityPropertySet;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.elster.jupiter.util.Checks.is;

/**
 * Validates the {@link SecurityPropertySetMustBeFromSameConfiguration} constraint against a {@link ComTaskEnablementImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (11:36)
 */
public class SecurityPropertySetMustBeFromSameConfigurationValidator implements ConstraintValidator<SecurityPropertySetMustBeFromSameConfiguration, ComTaskEnablementImpl> {

    @Override
    public void initialize(SecurityPropertySetMustBeFromSameConfiguration constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskEnablementImpl comTaskEnablement, ConstraintValidatorContext context) {
        SecurityPropertySet securityPropertySet = comTaskEnablement.getSecurityPropertySet();
        if (securityPropertySet != null && this.notSameConfiguration(securityPropertySet, comTaskEnablement)) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(ComTaskEnablementImpl.Fields.SECURITY_PROPERTY_SET.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean notSameConfiguration(SecurityPropertySet securityPropertySet, ComTaskEnablementImpl comTaskEnablement) {
        long securityPropertySetConfigurationId = securityPropertySet.getDeviceConfiguration().getId();
        long configurationId = comTaskEnablement.getDeviceConfiguration().getId();
        return !is(configurationId).equalTo(securityPropertySetConfigurationId);
    }

}