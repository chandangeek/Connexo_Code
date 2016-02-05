package com.elster.jupiter.servicecalls.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return service.newCustomStateTransitionEventType(symbol);
    }

}