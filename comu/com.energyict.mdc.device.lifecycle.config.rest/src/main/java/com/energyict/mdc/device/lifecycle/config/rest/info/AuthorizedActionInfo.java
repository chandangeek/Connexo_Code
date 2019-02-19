/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.MicroAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizedActionInfo {

    public long id;
    public String name;
    public DeviceLifeCycleStateInfo fromState;
    public DeviceLifeCycleStateInfo toState;
    public List<DeviceLifeCyclePrivilegeInfo> privileges;
    public StateTransitionEventTypeInfo triggeredBy;
    public Set<MicroActionAndCheckInfo> microActions;
    public Set<MicroActionAndCheckInfo> microChecks;
    public long version;
    public VersionInfo<Long> parent;

    public AuthorizedActionInfo() {
    }

    @JsonIgnore
    public Set<AuthorizedAction.Level> getPrivilegeLevels() {
        Set<AuthorizedAction.Level> levels = EnumSet.noneOf(AuthorizedAction.Level.class);
        if (this.privileges != null) {
            this.privileges.stream()
                    .map(each -> AuthorizedAction.Level.valueOf(each.privilege))
                    .forEach(levels::add);
        }
        return levels;
    }

    @JsonIgnore
    public Set<MicroAction> getMicroActions() {
        Set<MicroAction> microActions = EnumSet.noneOf(MicroAction.class);
        if (this.microActions != null) {
            this.microActions.stream()
                    .filter(candidate -> candidate.checked != null && candidate.checked)
                    .map(each -> MicroAction.valueOf(each.key))
                    .forEach(microActions::add);
        }
        return microActions;
    }

    @JsonIgnore
    public Set<String> getMicroChecks() {
        Set<String> microChecks = new HashSet<>();
        if (this.microChecks != null) {
            this.microChecks.stream()
                    .filter(candidate -> candidate.checked != null && candidate.checked)
                    .forEach(microCheck -> {
                        if (MicroActionAndCheckInfoFactory.CONSOLIDATED_MICRO_CHECKS_KEY.contains(microCheck.key)) {
                            microChecks.addAll(MicroActionAndCheckInfoFactory.CONSOLIDATED_MICRO_CHECKS);
                        } else {
                            microChecks.add(microCheck.key);
                        }
                    });
        }
        return microChecks;
    }

    public boolean isLinkedTo(StateTransition candidate) {
        if (this.fromState != null && this.toState != null && this.triggeredBy != null) {
            return candidate.getEventType().getSymbol().equals(this.triggeredBy.symbol)
                    && candidate.getFrom().getId() == this.fromState.id
                    && candidate.getTo().getId() == this.toState.id;
        }
        return false;
    }

    @JsonIgnore
    public String getEventTypeSymbol() {
        return this.triggeredBy != null ? this.triggeredBy.symbol : null;
    }
}