/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;

import java.time.Instant;
import java.util.Optional;

/**
 * Check if at least one connection is available on the device with the status: "Active".
 */
public class MetrologyConfigurationInCorrectStateIfAny extends TranslatableServerMicroCheck {

    public MetrologyConfigurationInCorrectStateIfAny(Thesaurus thesaurus){
       super(thesaurus);
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        throw new IllegalArgumentException("State cannot be null");
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp, State state) {
        if (!isValidToStage(state, device, effectiveTimestamp)) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY,
                            MicroCheck.METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY));
        }
        else {
            return Optional.empty();
        }
    }

    public boolean isValidToStage(State state, Device device, Instant effective) {
        if(state.getStage().isPresent()) {
            Stage stage = state.getStage().get();
            if(!EndDeviceStage.fromKey(stage.getName()).equals(EndDeviceStage.OPERATIONAL)) {
                return validateNoEffectiveMetrologyConfiguration(effective, device);
            }
        }
        return true;
    }

    private boolean validateNoEffectiveMetrologyConfiguration(Instant effective, Device device) {
        Optional<? extends MeterActivation> optionalMeterActivation = device.getMeterActivation(effective);
        if (optionalMeterActivation.isPresent()) {
            Optional<UsagePoint> usagePoint = optionalMeterActivation.get().getUsagePoint();
            if (usagePoint.isPresent()) {
                if(usagePoint.isPresent()) {
                    if (usagePoint.get().getEffectiveMetrologyConfiguration(effective).isPresent() && !UsagePointStage.POST_OPERATIONAL.getKey().equals(usagePoint.get().getState().getStage().get().getName())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
