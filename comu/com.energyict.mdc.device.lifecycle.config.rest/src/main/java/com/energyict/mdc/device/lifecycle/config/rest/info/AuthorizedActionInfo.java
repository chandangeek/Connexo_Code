package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizedActionInfo {
    public long id;
    public String name;
    public DeviceLifeCycleStateInfo fromState;
    public DeviceLifeCycleStateInfo toState;
    public List<DeviceLifeCyclePrivilegeInfo> privileges;
    public StateTransitionEventTypeInfo triggeredBy;
    public long version;

    public AuthorizedActionInfo() {}

    public AuthorizedActionInfo(Thesaurus thesaurus, AuthorizedAction action) {
        this.id = action.getId();
        this.privileges = action.getLevels().stream()
                .map(lvl -> new DeviceLifeCyclePrivilegeInfo(thesaurus, lvl))
                .collect(Collectors.toList());
        this.version = action.getVersion();
        if (action instanceof AuthorizedTransitionAction){
            fromBasicAction(thesaurus, (AuthorizedTransitionAction) action);
        } else {
            fromBpmAction((AuthorizedBusinessProcessAction) action);
        }
    }

    private void fromBasicAction(Thesaurus thesaurus, AuthorizedTransitionAction action){
        String name = action.getStateTransition().getFrom().getName() + action.getStateTransition().getEventType().getSymbol();
        this.name = thesaurus.getString(name, name);
        this.fromState = new DeviceLifeCycleStateInfo(thesaurus, action.getStateTransition().getFrom());
        this.toState = new DeviceLifeCycleStateInfo(thesaurus, action.getStateTransition().getTo());
        this.triggeredBy = new StateTransitionEventTypeFactory(thesaurus).from(action.getStateTransition().getEventType());
    }

    private void fromBpmAction(AuthorizedBusinessProcessAction action){
        this.name = action.getName();
    }

    @JsonIgnore
    public Set<AuthorizedAction.Level> getPrivilegeLevels(){
        Set<AuthorizedAction.Level> levels = EnumSet.noneOf(AuthorizedAction.Level.class);
        if (this.privileges != null){
            this.privileges.stream()
                .map(each -> AuthorizedAction.Level.valueOf(each.privilege))
                .forEach(levels::add);
        }
        return levels;
    }

    public boolean isLinkedTo(StateTransition candidate){
        if (this.fromState != null && this.toState != null){
            return candidate.getFrom().getId() == this.fromState.id && candidate.getTo().getId() == this.toState.id;
        }
        return false;
    }

    @JsonIgnore
    public String getEventTypeSymbol(){
        return this.triggeredBy != null ? this.triggeredBy.symbol : null;
    }
}
