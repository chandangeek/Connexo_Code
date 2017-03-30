/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import java.util.List;

public class StateTransitionEventTypeInfo {

    public String symbol;
    public String name;
    public List<TransitionBusinessProcessInfo> onEntry;
    public List<TransitionBusinessProcessInfo> onExit;
    public String context;

    public StateTransitionEventTypeInfo() {
    }

    public StateTransitionEventTypeInfo(String symbol, String name, String context) {
        this.symbol = symbol;
        this.name = name;
        this.context = context;
    }
}
