/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public class NoActiveServiceCalls extends TranslatableServerMicroCheck {

    private final ServiceCallService serviceCallService;

    public NoActiveServiceCalls(Thesaurus thesaurus, ServiceCallService serviceCallService) {
        super(thesaurus);
        this.serviceCallService = serviceCallService;
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.NO_ACTIVE_SERVICE_CALLS;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp, State state) {
        if (hasActiveServiceCalls(device)) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.NO_ACTIVE_SERVICE_CALLS,
                            MicroCheck.NO_ACTIVE_SERVICE_CALLS));
        } else {
            return Optional.empty();
        }
    }

    private boolean hasActiveServiceCalls(Device device) {
        return !activeServiceCalls(device).isEmpty();
    }

    private Set<ServiceCall> activeServiceCalls(Device device) {
        return serviceCallService.findServiceCalls(device, serviceCallService.nonFinalStates());
    }

}
