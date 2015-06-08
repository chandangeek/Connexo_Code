package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Thesaurus;

public class StateTransitionEventTypeInfo {

    public String symbol;
    public String name;

    public StateTransitionEventTypeInfo() {
    }

    public StateTransitionEventTypeInfo(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }
}
