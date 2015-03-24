package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.Objects;

public class LifeCycleStateTransitionFactory {

    private final Thesaurus thesaurus;

    @Inject
    public LifeCycleStateTransitionFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public LifeCycleStateTransitionInfo from(StateTransition transition){
        Objects.requireNonNull(transition);
        return new LifeCycleStateTransitionInfo(thesaurus, transition);
    }
}
