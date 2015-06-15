package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.MicroAction;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizedActionInfoFactory {

    private final Thesaurus thesaurus;
    private final MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory;

    @Inject
    public AuthorizedActionInfoFactory(Thesaurus thesaurus, MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory) {
        this.thesaurus = thesaurus;
        this.microActionAndCheckInfoFactory = microActionAndCheckInfoFactory;
    }

    public AuthorizedActionInfo from(AuthorizedAction action){
        Objects.requireNonNull(action);
        AuthorizedActionInfo info = new AuthorizedActionInfo();
        info.id = action.getId();
        info.privileges = action.getLevels().stream()
                .map(lvl -> new DeviceLifeCyclePrivilegeInfo(thesaurus, lvl))
                .collect(Collectors.toList());
        info.version = action.getVersion();
        if (action instanceof AuthorizedTransitionAction){
            fromBasicAction(info, (AuthorizedTransitionAction) action);
        } else {
            fromBpmAction(info, (AuthorizedBusinessProcessAction) action);
        }
        return info;
    }


    private void fromBasicAction(AuthorizedActionInfo info, AuthorizedTransitionAction action){
        info.name = action.getStateTransition().getName(thesaurus);
        info.fromState = new DeviceLifeCycleStateInfo(thesaurus, action.getStateTransition().getFrom());
        info.toState = new DeviceLifeCycleStateInfo(thesaurus, action.getStateTransition().getTo());
        info.triggeredBy = new StateTransitionEventTypeInfo(thesaurus, action.getStateTransition().getEventType());
        Set<MicroAction> microActions = action.getActions();
        if (!microActions.isEmpty()){
            info.microActions = new ArrayList<>(microActions.size());
            for (MicroAction microAction : microActions) {
                MicroActionAndCheckInfo microActionInfo = microActionAndCheckInfoFactory.optional(microAction);
                microActionInfo.checked = true;
                info.microActions.add(microActionInfo);
            }
        }
    }

    private void fromBpmAction(AuthorizedActionInfo info, AuthorizedBusinessProcessAction action){
        info.name = action.getName();
    }
}
