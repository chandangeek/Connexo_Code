/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.parent;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

public abstract class AbstractServiceCallHandler implements ServiceCallHandler {

    protected abstract void process(ServiceCall serviceCall);

    @Override
    public final void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
        case PENDING:
            serviceCall.requestTransition(DefaultState.ONGOING);
            break;
        case ONGOING:
            process(serviceCall);
            break;
        case SUCCESSFUL:
        case FAILED:
        default:
            break;
        }
    }

}
