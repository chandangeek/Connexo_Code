/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class UsagePointLifeCycleStateInfoFactory {

    private final BusinessProcessInfoFactory bpmFactory;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointLifeCycleStateInfoFactory(BusinessProcessInfoFactory bpmFactory, Thesaurus thesaurus) {
        this.bpmFactory = bpmFactory;
        this.thesaurus = thesaurus;
    }

    public UsagePointLifeCycleStateInfo from(UsagePointLifeCycle lifeCycle, State state) {
        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = state.getId();
        info.name = thesaurus.getString(state.getName(), state.getName());
        info.version = state.getVersion();
        info.isInitial = state.isInitial();
        info.stage = thesaurus.getString(state.getStage().get().getName(), state.getStage().get().getName());
        info.stageName = state.getStage().get().getName();
        info.parent = new VersionInfo<>(lifeCycle.getId(), lifeCycle.getVersion());
        return info;
    }

    public UsagePointLifeCycleStateInfo fullInfo(UsagePointLifeCycle lifeCycle, State state) {
        UsagePointLifeCycleStateInfo info = from(lifeCycle, state);
        info.onEntry = state.getOnEntryProcesses().stream().map(ProcessReference::getStateChangeBusinessProcess)
                .map(this.bpmFactory::from).collect(Collectors.toList());
        info.onExit = state.getOnExitProcesses().stream().map(ProcessReference::getStateChangeBusinessProcess)
                .map(this.bpmFactory::from).collect(Collectors.toList());
        return info;
    }
}
