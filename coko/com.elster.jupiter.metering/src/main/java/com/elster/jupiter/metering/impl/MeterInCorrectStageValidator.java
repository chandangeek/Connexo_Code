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
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.util.Optional;

public class MeterInCorrectStageValidator implements ConstraintValidator<MeterInCorrectStage, MeterActivationImpl> {

    private ConstraintValidatorContext context;
    private final Thesaurus thesaurus;

    @Inject
    public MeterInCorrectStageValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(MeterInCorrectStage meterInCorrectStage) {

    }

    @Override
    public boolean isValid(MeterActivationImpl meterActivation, ConstraintValidatorContext context) {
        this.context = context;
        Instant activationTime = meterActivation.getStart();
        Optional<Meter> meter = meterActivation.getMeter();
        Optional<UsagePoint> usagePoint = meterActivation.getUsagePoint();
        return !meter.isPresent() || !usagePoint.isPresent() || isValid(meter.get(), usagePoint.get(), activationTime);
    }

    private boolean isValid(Meter meter, UsagePoint usagePoint, Instant activationTime) {
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration(activationTime);
        return isValid(meter, metrologyConfiguration, activationTime, usagePoint.getState(activationTime).getStage());
    }

    private boolean isValid(Meter meter, Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration, Instant activationTime, Optional<Stage> usagePointStage) {
        Optional<State> state = meter.getState(activationTime);
        if (!state.isPresent()) {
            return true;
        }
        if (!state.get().getStage().isPresent() || !usagePointStage.isPresent()) {
            addContextValidationError("No stage present");
            return false;
        }
        Stage stage = state.get().getStage().get();
        if (metrologyConfiguration.isPresent() && usagePointStage.get().getName().equals(UsagePointStage.OPERATIONAL.getKey()) && !stage.getName().equals(EndDeviceStage.OPERATIONAL.getKey())) {
            addContextValidationError(getErrorMessage(PrivateMessageSeeds.METER_NOT_IN_OPERATIONAL_STAGE));
            return false;
        } else if (!metrologyConfiguration.isPresent() && stage.getName().equals(EndDeviceStage.POST_OPERATIONAL.getKey())) {
            addContextValidationError(getErrorMessage(PrivateMessageSeeds.METER_IN_POST_OPERATIONAL_STAGE));
            return false;
        }
        return true;
    }

    private void addContextValidationError(String message) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(message)
            .addPropertyNode("stage")
            .addConstraintViolation();
    }

    private String getErrorMessage(MessageSeed seed) {
        return this.thesaurus.getFormat(seed).format();
    }
}
