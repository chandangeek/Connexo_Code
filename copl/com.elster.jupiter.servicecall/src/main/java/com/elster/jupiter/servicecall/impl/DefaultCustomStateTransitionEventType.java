package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.servicecall.ServiceCallService;

/**
 * Models the default {@link CustomStateTransitionEventType}
 * that are necessary to create the default service call life cycle.
 */
public enum DefaultCustomStateTransitionEventType {
    ENQUEUED("#enqueued"),
    SCHEDULED("#scheduled"),
    PENDING("#pending"),
    PAUSED("#paused"),
    WAITING("#waiting"),
    ONGOING("#ongoing"),
    CANCELLED("#cancelled"),
    PARTIAL_SUCCESS("#partialsuccess"),
    SUCCESSFUL("#successful"),
    FAILED("#failed"),
    REJECTED("#rejected");

    private String symbol;

    DefaultCustomStateTransitionEventType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public CustomStateTransitionEventType findOrCreate(FiniteStateMachineService service) {
        return service
                .findCustomStateTransitionEventType(this.symbol)
                .orElseGet(() -> this.createNewStateTransitionEventType(service, this.symbol));
    }

    private CustomStateTransitionEventType createNewStateTransitionEventType(FiniteStateMachineService service, String symbol) {
        return service.newCustomStateTransitionEventType(symbol, ServiceCallService.COMPONENT_NAME);
    }

}