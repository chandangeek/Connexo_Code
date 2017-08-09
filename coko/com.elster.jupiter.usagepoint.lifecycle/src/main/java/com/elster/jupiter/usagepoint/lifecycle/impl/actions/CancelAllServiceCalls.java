/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

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
    protected Set<DefaultTransition> getTransitionCandidates() {
        return EnumSet.of( DefaultTransition.ACTIVATE, DefaultTransition.DEACTIVATE, DefaultTransition.INSTALL_ACTIVE, DefaultTransition.INSTALL_INACTIVE);
    }

    @Override
    public boolean isVisibleByDefault(State fromState, State toState) {
        if((fromState.getName().equals(DefaultState.ACTIVE) && toState.getName().equals(DefaultState.DEMOLISHED)) ||
                (fromState.getName().equals(DefaultState.INACTIVE) && toState.getName().equals(DefaultState.DEMOLISHED)))
            return true;
        else
            return false;
    }

    @Override
    public boolean isMandatoryForTransition(State fromState, State toState) {
        return false;
    }
}
