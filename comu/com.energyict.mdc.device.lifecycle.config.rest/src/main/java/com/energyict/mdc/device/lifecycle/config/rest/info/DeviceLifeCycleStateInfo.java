/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleStateInfo {
    public long id;
    public String name;
    public boolean isCustom;
    public boolean isInitial;
    public long version;
    public List<TransitionBusinessProcessInfo> onEntry = new ArrayList<>();
    public List<TransitionBusinessProcessInfo> onExit = new ArrayList<>();
    public VersionInfo<Long> parent;

    public DeviceLifeCycleStateInfo() {}

    public DeviceLifeCycleStateInfo(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, DeviceLifeCycle deviceLifeCycle, State state) {
        super();
        this.id = state.getId();
        this.isCustom = state.isCustom();
        this.isInitial = state.isInitial();
        this.version = state.getVersion();
        this.name = DefaultState.from(state).map(deviceLifeCycleConfigurationService::getDisplayName).orElseGet(state::getName);
        if (deviceLifeCycle != null) {
            this.parent = new VersionInfo<>(deviceLifeCycle.getId(), deviceLifeCycle.getVersion());
        }
        addAllBusinessProcessInfos(onEntry, state.getOnEntryProcesses());
        addAllBusinessProcessInfos(onExit, state.getOnExitProcesses());
    }

    private void addAllBusinessProcessInfos(List<TransitionBusinessProcessInfo> target, List<ProcessReference> source){
        source.stream()
                .map(ProcessReference::getStateChangeBusinessProcess)
                .map(x -> new TransitionBusinessProcessInfo(x.getId(), x.getName(), x.getDeploymentId(), x.getProcessId()))
                .forEach(target::add);
    }
}
