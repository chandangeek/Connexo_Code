/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Checks that all collected data in both load profiles and registers is valid.
 * The actual check is done by comparing the last reading timestamp
 * of all profiles and registers against the last checked.
 */
public class AllDataValid extends TranslatableServerMicroCheck {

    private ValidationService validationService;

    @Inject
    public final void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.VALIDATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp, State toState) {
        Optional<? extends MeterActivation> current = device.getCurrentMeterActivation();
        return !current.isPresent() || validationService.validationEnabled(current.get().getMeter().get()) &&
                validationService.getEvaluator().areSuspectsPresent(Collections.singleton(QualityCodeSystem.MDC),
                        current.get().getChannelsContainer()) ?
                fail(MicroCheckTranslations.Message.ALL_DATA_VALID) : Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.of(
                DefaultTransition.DEACTIVATE,
                DefaultTransition.DEACTIVATE_AND_DECOMMISSION,
                DefaultTransition.DECOMMISSION);
    }
}
