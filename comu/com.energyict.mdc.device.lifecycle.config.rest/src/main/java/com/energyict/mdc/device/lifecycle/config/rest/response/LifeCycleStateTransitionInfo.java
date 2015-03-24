package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;

public class LifeCycleStateTransitionInfo extends IdWithNameInfo {
    public LifeCycleStateInfo from;
    public LifeCycleStateInfo to;

    public LifeCycleStateTransitionInfo() {}

    public LifeCycleStateTransitionInfo(Thesaurus thesaurus, StateTransition transition) {
        super(transition.getId(), thesaurus.getString("", "")); // TODO replace the empty string by transition name
        this.from = new LifeCycleStateInfo(transition.getFrom());
        this.to = new LifeCycleStateInfo(transition.getTo());
    }
}
