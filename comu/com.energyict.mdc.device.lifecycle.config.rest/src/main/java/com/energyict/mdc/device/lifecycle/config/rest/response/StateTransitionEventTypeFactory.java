package com.energyict.mdc.device.lifecycle.config.rest.response;

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
        return new StateTransitionEventTypeInfo(thesaurus, eventType);
    }
}
