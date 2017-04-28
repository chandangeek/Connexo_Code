/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class UsagePointLifeCycleInfoFactory {
    private final UsagePointLifeCycleStateInfoFactory stateInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointLifeCycleInfoFactory(UsagePointLifeCycleStateInfoFactory stateInfoFactory, Thesaurus thesaurus) {
        this.stateInfoFactory = stateInfoFactory;
        this.thesaurus = thesaurus;
    }

    public UsagePointLifeCycleInfo shortInfo(UsagePointLifeCycle lifeCycle) {
        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = lifeCycle.getId();
        info.name = thesaurus.getString(lifeCycle.getName(), lifeCycle.getName());
        info.version = lifeCycle.getVersion();
        info.obsolete = lifeCycle.isObsolete();
        info.isDefault = lifeCycle.isDefault();
        return info;
    }

    public UsagePointLifeCycleInfo from(UsagePointLifeCycle lifeCycle) {
        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = lifeCycle.getId();
        info.name = this.thesaurus.getString(lifeCycle.getName(), lifeCycle.getName());
        info.version = lifeCycle.getVersion();
        info.obsolete = lifeCycle.isObsolete();
        info.isDefault = lifeCycle.isDefault();
        info.states = lifeCycle.getStates().stream().map(state -> this.stateInfoFactory.from(lifeCycle, state)).collect(Collectors.toList());
        info.transitionsCount = lifeCycle.getTransitions().size();
        return info;
    }
}
