/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates the {@link MeterRolePartOfMetrologyConfigurationIfAny} constraint against a {@link MeterActivationImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-22 (13:02)
 */
class MeterRolePartOfMetrologyConfigurationIfAnyValidator implements ConstraintValidator<MeterRolePartOfMetrologyConfigurationIfAny, MeterActivationImpl> {

    private final Thesaurus thesaurus;

    @Inject
    MeterRolePartOfMetrologyConfigurationIfAnyValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(MeterRolePartOfMetrologyConfigurationIfAny meterRolePartOfMetrologyConfigurationIfAny) {
        // No need to store the annotation yet
    }

    @Override
    public boolean isValid(MeterActivationImpl meterActivation, ConstraintValidatorContext context) {
        Optional<UsagePoint> usagePoint = meterActivation.getUsagePoint();
        return !usagePoint.isPresent() || this.validateMeterRolePartOfConfigurations(meterActivation, usagePoint.get(), context);
    }

    private boolean validateMeterRolePartOfConfigurations(MeterActivationImpl meterActivation, UsagePoint usagePoint, ConstraintValidatorContext context) {
        Optional<MeterRole> meterRole = meterActivation.getMeterRole();
        return !meterRole.isPresent() || this.validateMeterRolePartOfConfigurations(meterRole.get(), meterActivation.getRange(), usagePoint, context);
    }

    private boolean validateMeterRolePartOfConfigurations(MeterRole meterRole, Range<Instant> period, UsagePoint usagePoint, ConstraintValidatorContext context) {
        Set<MeterRole> effectiveMeterRoles = usagePoint
                .getEffectiveMetrologyConfigurations(period)
                .stream()
                .map(emc -> emc.getMetrologyConfiguration().getMeterRoles())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        if (effectiveMeterRoles.contains(meterRole)) {
            return true;
        } else {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(getErrorMessage(meterRole))
                .addPropertyNode("meterRole")
                .addConstraintViolation();
            return false;
        }
    }

    private String getErrorMessage(MeterRole meterRole) {
        return this.thesaurus
                .getFormat(MessageSeeds.METER_ROLE_NOT_IN_CONFIGURATION)
                .format(meterRole.getDisplayName());
    }

}