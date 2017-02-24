/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.util.Optional;

public class MeterInCorrectStageValidator implements ConstraintValidator<MeterInCorrectStage, MeterActivationImpl> {

    private ConstraintValidatorContext context;

    @Override
    public void initialize(MeterInCorrectStage meterInCorrectStage) {

    }

    @Override
    public boolean isValid(MeterActivationImpl meterActivation, ConstraintValidatorContext context) {
        this.context = context;
        Instant activationTime = meterActivation.getStart();
        Optional<Meter> meter = meterActivation.getMeter();
        return !meter.isPresent() || isValid(meter.get(), activationTime);

    }

    private boolean isValid(Meter meter, Instant activationTime) {
        Optional<UsagePoint> usagePoint = meter.getUsagePoint(activationTime);
        return !usagePoint.isPresent() || isValid(meter, usagePoint.get(), activationTime);
    }

    private boolean isValid(Meter meter, UsagePoint usagePoint, Instant activationTime) {
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration(activationTime);
        return isValid(meter, metrologyConfiguration, activationTime);
    }

    private boolean isValid(Meter meter, Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration, Instant activationTime) {
        Optional<State> state = meter.getState(activationTime);
        if(!state.isPresent()) {
            return true;
        }
        if (!state.get().getStage().isPresent()) {
            addContextValidationError("No stage present");
            return false;
        }
        Stage stage = state.get().getStage().get();
        if(metrologyConfiguration.isPresent() && !stage.getName().equals(EndDeviceStage.OPERATIONAL.name())) {
            addContextValidationError("Metrology configuration is active but stage is not operational");
            return false;
        } else if(!metrologyConfiguration.isPresent() && stage.getName().equals(EndDeviceStage.POST_OPERATIONAL.name())) {
            addContextValidationError("Metrology configuration is not active but stage is post-operational");
            return false;
        }
        return true;
    }

    private void addContextValidationError(String message) {
        context.disableDefaultConstraintViolation();
        context
                .buildConstraintViolationWithTemplate(message)
                .addPropertyNode("state")
                .addConstraintViolation();
    }
}
