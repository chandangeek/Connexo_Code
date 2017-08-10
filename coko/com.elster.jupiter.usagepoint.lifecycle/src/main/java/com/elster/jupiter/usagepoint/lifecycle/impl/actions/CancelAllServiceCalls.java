/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;

public class CancelAllServiceCalls extends TranslatableAction {
   private final ServiceCallService serviceCallService;

    @Inject
    public CancelAllServiceCalls(ServiceCallService serviceCallService) {
          this.serviceCallService = serviceCallService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.MONITORING.name();
    }

    @Override
    protected void doExecute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) {
        serviceCallService.cancelServiceCallsFor(usagePoint);
    }

    @Override
    public boolean isAvailableByDefault(State fromState, State toState) {
        if ((fromState.getName().equals(DefaultState.ACTIVE.getTranslation().getKey()) && toState.getName()
                .equals(DefaultState.DEMOLISHED.getTranslation().getKey())) ||
                (fromState.getName().equals(DefaultState.INACTIVE.getTranslation().getKey()) && toState.getName()
                        .equals(DefaultState.DEMOLISHED.getTranslation().getKey())))
            return true;
        else
            return false;
    }

    @Override
    public boolean isMandatoryForTransition(State fromState, State toState) {
        return false;
    }
}
