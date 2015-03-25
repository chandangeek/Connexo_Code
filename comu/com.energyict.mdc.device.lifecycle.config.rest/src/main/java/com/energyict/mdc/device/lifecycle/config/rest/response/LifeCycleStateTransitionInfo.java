package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LifeCycleStateTransitionInfo extends IdWithNameInfo {
    public LifeCycleStateInfo fromState;
    public LifeCycleStateInfo toState;
    public List<LifeCyclePrivilegeInfo> privileges;

    public LifeCycleStateTransitionInfo() {}

    public LifeCycleStateTransitionInfo(Thesaurus thesaurus, AuthorizedAction action) {
        super();
        this.id = action.getId();
        this.privileges = action.getLevels().stream().map(lvl -> new LifeCyclePrivilegeInfo(thesaurus, lvl)).collect(Collectors.toList());
        if (action instanceof AuthorizedTransitionAction){
            fromBasicAction(thesaurus, (AuthorizedTransitionAction) action);
        } else {
            fromBpmAction(thesaurus, (AuthorizedBusinessProcessAction) action);
        }
    }

    private void fromBasicAction(Thesaurus thesaurus, AuthorizedTransitionAction action){
        this.name = action.getStateTransition().getName(thesaurus);
        this.fromState = new LifeCycleStateInfo(action.getStateTransition().getFrom());
        this.toState = new LifeCycleStateInfo(action.getStateTransition().getTo());
    }

    private void fromBpmAction(Thesaurus thesaurus, AuthorizedBusinessProcessAction action){
        this.name = action.getName();
    }
}
