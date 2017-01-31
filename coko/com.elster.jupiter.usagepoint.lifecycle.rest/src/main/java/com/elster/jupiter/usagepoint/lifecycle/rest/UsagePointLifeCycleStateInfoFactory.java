/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class UsagePointLifeCycleStateInfoFactory {

    private final BusinessProcessInfoFactory bpmFactory;

    @Inject
    public UsagePointLifeCycleStateInfoFactory(BusinessProcessInfoFactory bpmFactory) {
        this.bpmFactory = bpmFactory;
    }

    public UsagePointLifeCycleStateInfo from(UsagePointState state) {
        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = state.getId();
        info.name = state.getName();
        info.version = state.getVersion();
        info.isInitial = state.isInitial();
        info.stage = state.getStage().getKey();
        info.parent = new VersionInfo<>(state.getLifeCycle().getId(), state.getLifeCycle().getVersion());
        return info;
    }

    public UsagePointLifeCycleStateInfo fullInfo(UsagePointState state) {
        UsagePointLifeCycleStateInfo info = from(state);
        info.onEntry = state.getOnEntryProcesses().stream().map(ProcessReference::getStateChangeBusinessProcess)
                .map(this.bpmFactory::from).collect(Collectors.toList());
        info.onExit = state.getOnExitProcesses().stream().map(ProcessReference::getStateChangeBusinessProcess)
                .map(this.bpmFactory::from).collect(Collectors.toList());
        return info;
    }
}
