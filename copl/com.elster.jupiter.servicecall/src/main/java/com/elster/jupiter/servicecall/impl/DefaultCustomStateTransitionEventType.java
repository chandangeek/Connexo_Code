/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;

/**
 * Models the default {@link CustomStateTransitionEventType}
 * that are necessary to create the default service call life cycle.
 */
enum DefaultCustomStateTransitionEventType {
    SCHEDULED("#scheduled", DefaultState.SCHEDULED),
    PENDING("#pending", DefaultState.PENDING),
    PAUSED("#paused", DefaultState.PAUSED),
    WAITING("#waiting", DefaultState.WAITING),
    ONGOING("#ongoing", DefaultState.ONGOING),
    CANCELLED("#cancelled", DefaultState.CANCELLED),
    PARTIAL_SUCCESS("#partialsuccess", DefaultState.PARTIAL_SUCCESS),
    SUCCESSFUL("#successful", DefaultState.SUCCESSFUL),
    FAILED("#failed", DefaultState.FAILED),
    REJECTED("#rejected", DefaultState.REJECTED);

    private final String symbol;
    private final DefaultState targetState;


    DefaultCustomStateTransitionEventType(String symbol, DefaultState targetState) {
        this.symbol = symbol;
        this.targetState = targetState;
    }

    String getSymbol() {
        return this.symbol;
    }

    CustomStateTransitionEventType findOrCreate(FiniteStateMachineService service) {
        return service
                .findCustomStateTransitionEventType(this.symbol)
                .orElseGet(() -> this.createNewStateTransitionEventType(service, this.symbol));
    }

    private CustomStateTransitionEventType createNewStateTransitionEventType(FiniteStateMachineService service, String symbol) {
        return service.newCustomStateTransitionEventType(symbol, ServiceCallService.COMPONENT_NAME);
    }

    DefaultState getTarget() {
        return targetState;
    }
}