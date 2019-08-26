/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.common.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class NoActiveServiceCalls extends TranslatableServerMicroCheck {

    private ServiceCallService serviceCallService;

    @Inject
    public final void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.MONITORING.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        return (hasActiveServiceCalls(device)) ?
                fail(MicroCheckTranslations.Message.NO_ACTIVE_SERVICE_CALLS) :
                Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.of(
                DefaultTransition.DEACTIVATE_AND_DECOMMISSION,
                DefaultTransition.DECOMMISSION);
    }

    private boolean hasActiveServiceCalls(Device device) {
        return !activeServiceCalls(device).isEmpty();
    }

    private Set<ServiceCall> activeServiceCalls(Device device) {
        return serviceCallService.findServiceCalls(device, serviceCallService.nonFinalStates());
    }
}
