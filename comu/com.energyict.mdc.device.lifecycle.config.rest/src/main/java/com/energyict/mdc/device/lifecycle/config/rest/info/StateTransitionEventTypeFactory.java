/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.Objects;

public class StateTransitionEventTypeFactory {

    private final Thesaurus thesaurus;

    @Inject
    public StateTransitionEventTypeFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public StateTransitionEventTypeInfo from(StateTransitionEventType eventType){
        Objects.requireNonNull(eventType);
        String symbol = eventType.getSymbol();
        String context = eventType.getContext();
        return new StateTransitionEventTypeInfo(symbol, thesaurus.getString(symbol, symbol), context);
    }
}
