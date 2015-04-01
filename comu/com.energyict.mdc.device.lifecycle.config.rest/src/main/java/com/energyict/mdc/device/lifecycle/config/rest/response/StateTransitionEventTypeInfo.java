package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Thesaurus;

public class StateTransitionEventTypeInfo {

    public String symbol;
    public String name;

    public StateTransitionEventTypeInfo() {
    }

    public StateTransitionEventTypeInfo(Thesaurus thesaurus, StateTransitionEventType eventType) {
        this.symbol = eventType.getSymbol();
        this.name = thesaurus.getString(eventType.getSymbol(), eventType.getSymbol());
    }
}
