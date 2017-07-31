package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;

/**
 * Created by H241414 on 7/21/2017.
 */
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
    public boolean isMandatoryForTransition(State fromState, State toState) {
        return false;
    }
}
