package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

import javax.inject.Inject;
import java.util.Objects;

public class LifeCycleStateTransitionFactory {

    private final Thesaurus thesaurus;

    @Inject
    public LifeCycleStateTransitionFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public LifeCycleStateTransitionInfo from(AuthorizedAction action){
        Objects.requireNonNull(action);
        return new LifeCycleStateTransitionInfo(thesaurus, action);
    }
}
