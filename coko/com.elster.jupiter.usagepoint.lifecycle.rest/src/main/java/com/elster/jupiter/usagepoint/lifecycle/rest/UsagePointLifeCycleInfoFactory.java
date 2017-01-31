/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class UsagePointLifeCycleInfoFactory {
    private final UsagePointLifeCycleStateInfoFactory stateInfoFactory;

    @Inject
    public UsagePointLifeCycleInfoFactory(UsagePointLifeCycleStateInfoFactory stateInfoFactory) {
        this.stateInfoFactory = stateInfoFactory;
    }

    public UsagePointLifeCycleInfo shortInfo(UsagePointLifeCycle lifeCycle) {
        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = lifeCycle.getId();
        info.name = lifeCycle.getName();
        info.version = lifeCycle.getVersion();
        info.obsolete = lifeCycle.isObsolete();
        info.isDefault = lifeCycle.isDefault();
        return info;
    }

    public UsagePointLifeCycleInfo from(UsagePointLifeCycle lifeCycle) {
        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = lifeCycle.getId();
        info.name = lifeCycle.getName();
        info.version = lifeCycle.getVersion();
        info.obsolete = lifeCycle.isObsolete();
        info.isDefault = lifeCycle.isDefault();
        info.states = lifeCycle.getStates().stream().map(this.stateInfoFactory::from).collect(Collectors.toList());
        info.transitionsCount = lifeCycle.getTransitions().size();
        return info;
    }
}
