package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;

/**
 * Models the default {@link CustomStateTransitionEventType}
 * that are necessary to create the default device life cycle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-24 (15:33)
 */
public enum DefaultCustomStateTransitionEventType {

    COMMISSIONING("#commissioning"),
    ACTIVATED("#activated"),
    DEACTIVATED("#deactivated"),
    DECOMMISSIONED("#decommissioned"),
    DELETED("#deleted"),
    RECYCLED("#recycled", false),
    REVOKED("#revoked", false);

    private String symbol;
    private boolean isStandard;

    DefaultCustomStateTransitionEventType(String symbol) {
        this(symbol,true);
    }
    // isStandard => used within the standard device life cycle
    DefaultCustomStateTransitionEventType(String symbol, boolean isStandard) {
        this.symbol = symbol;
        this.isStandard = isStandard;
    }

    public String getSymbol() {
        return this.symbol;
    }
    public boolean isStandardEventType(){
        return isStandard;
    }

    public CustomStateTransitionEventType findOrCreate(FiniteStateMachineService service) {
        return service
                .findCustomStateTransitionEventType(this.symbol)
                .orElseGet(() -> this.createNewStateTransitionEventType(service, this.symbol));
    }

    private CustomStateTransitionEventType createNewStateTransitionEventType(FiniteStateMachineService service, String symbol) {
        CustomStateTransitionEventType eventType = service.newCustomStateTransitionEventType(symbol);
        eventType.save();
        return eventType;
    }

}