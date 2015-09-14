package com.energyict.mdc.device.lifecycle.config.rest.info;

import java.util.List;

public class StateTransitionEventTypeInfo {

    public String symbol;
    public String name;
    public List<TransitionBusinessProcessInfo> onEntry;
    public List<TransitionBusinessProcessInfo> onExit;

    public StateTransitionEventTypeInfo() {
    }

    public StateTransitionEventTypeInfo(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }
}
