package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

import javax.inject.Inject;
import java.util.Objects;

public class LifeCycleStateFactory {

    private final Thesaurus thesaurus;

    @Inject
    public LifeCycleStateFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public LifeCycleStateInfo from(State state){
        Objects.requireNonNull(state);
        return new LifeCycleStateInfo(thesaurus, state);
    }
}
