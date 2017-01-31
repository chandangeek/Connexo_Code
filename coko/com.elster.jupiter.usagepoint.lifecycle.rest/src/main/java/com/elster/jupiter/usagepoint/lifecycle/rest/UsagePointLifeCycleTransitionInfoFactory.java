/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class UsagePointLifeCycleTransitionInfoFactory {

    private final UsagePointLifeCycleStateInfoFactory stateInfoFactory;
    private final UsagePointLifeCyclePrivilegeInfoFactory privilegeInfoFactory;
    private final MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory;

    @Inject
    public UsagePointLifeCycleTransitionInfoFactory(UsagePointLifeCycleStateInfoFactory stateInfoFactory,
                                                    UsagePointLifeCyclePrivilegeInfoFactory privilegeInfoFactory,
                                                    MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory) {
        this.stateInfoFactory = stateInfoFactory;
        this.privilegeInfoFactory = privilegeInfoFactory;
        this.microActionAndCheckInfoFactory = microActionAndCheckInfoFactory;
    }

    public UsagePointLifeCycleTransitionInfo from(UsagePointTransition transition) {
        UsagePointLifeCycleTransitionInfo info = new UsagePointLifeCycleTransitionInfo();
        info.id = transition.getId();
        info.name = transition.getName();
        info.version = transition.getVersion();
        info.fromState = this.stateInfoFactory.from(transition.getFrom());
        info.toState = this.stateInfoFactory.from(transition.getTo());
        info.parent = new VersionInfo<>(transition.getLifeCycle().getId(), transition.getLifeCycle().getVersion());
        return info;
    }

    public UsagePointLifeCycleTransitionInfo fullInfo(UsagePointTransition transition) {
        UsagePointLifeCycleTransitionInfo info = from(transition);
        info.privileges = transition.getLevels().stream().map(this.privilegeInfoFactory::from).collect(Collectors.toList());
        info.microActions = transition.getActions().stream()
                .map(this.microActionAndCheckInfoFactory::optional)
                .map(action -> {
                    action.checked = true;
                    return action;
                })
                .collect(Collectors.toSet());
        info.microChecks = transition.getChecks().stream()
                .map(this.microActionAndCheckInfoFactory::optional)
                .map(check -> {
                    check.checked = true;
                    return check;
                })
                .collect(Collectors.toSet());
        return info;
    }
}
