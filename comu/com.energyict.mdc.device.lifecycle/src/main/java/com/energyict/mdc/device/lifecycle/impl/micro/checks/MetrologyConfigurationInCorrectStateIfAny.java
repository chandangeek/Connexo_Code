/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MetrologyConfigurationInCorrectStateIfAny extends TranslatableServerMicroCheck {
    private static final String INSIGHT_LICENSE = "INS";
    private final LicenseService licenseService;

    @Inject
    public MetrologyConfigurationInCorrectStateIfAny(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.INSTALLATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State state) {
        return licenseService.getLicensedApplicationKeys().contains(INSIGHT_LICENSE)
                && !isValidToStage(state, device, effectiveTimestamp) ?
                fail(MicroCheckTranslations.Message.METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY) :
                Optional.empty();
    }

    private boolean isValidToStage(State state, Device device, Instant effective) {
        if (state.getStage().isPresent()) {
            Stage stage = state.getStage().get();
            if (!EndDeviceStage.fromKey(stage.getName()).equals(EndDeviceStage.OPERATIONAL)) {
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
                if (usagePoint.get().getEffectiveMetrologyConfiguration(effective).isPresent() && !UsagePointStage.POST_OPERATIONAL.getKey().equals(usagePoint.get().getState().getStage().get().getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Set<DefaultTransition> getRequiredDefaultTransitions() {
        List<String> licensedApplicationKeys = licenseService.getLicensedApplicationKeys();
        return licensedApplicationKeys.isEmpty() // For the case of installation because no license info is available at that time.
                // The check is not harmful since also verifies license in execute method, so it's better to turn it on.
                || licensedApplicationKeys.contains(INSIGHT_LICENSE) ?
                EnumSet.allOf(DefaultTransition.class) :
                EnumSet.noneOf(DefaultTransition.class);
    }

    @Override
    public boolean isOptionalForTransition(State fromState, State toState) {
        return licenseService.getLicensedApplicationKeys().contains(INSIGHT_LICENSE);
    }
}
