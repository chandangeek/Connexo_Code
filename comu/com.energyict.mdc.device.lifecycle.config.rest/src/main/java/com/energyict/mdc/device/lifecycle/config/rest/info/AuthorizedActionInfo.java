package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
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
    public List<MicroActionAndCheckInfo> microActions;
    public long version;

    public AuthorizedActionInfo() {}

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

    @JsonIgnore
    public Set<MicroAction> getMicroActions(){
        Set<MicroAction> microActions = EnumSet.noneOf(MicroAction.class);
        if (this.microActions != null){
            this.microActions.stream()
                    .filter(candidate -> candidate.checked != null && candidate.checked)
                    .map(each -> MicroAction.valueOf(each.key))
                    .forEach(microActions::add);
        }
        return microActions;
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
