/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.metering.DefaultState;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleStateInfo {
    public long id;
    public String name;
    public boolean isCustom;
    public boolean isInitial;
    public long version;
    public IdWithNameInfo stage;
    public List<TransitionBusinessProcessInfo> onEntry = new ArrayList<>();
    public List<TransitionBusinessProcessInfo> onExit = new ArrayList<>();
    public List<TransitionEndPointConfigurationInfo> onEntryEndPointConfigurations = new ArrayList<>();
    public List<TransitionEndPointConfigurationInfo> onExitEndPointConfigurations = new ArrayList<>();
    public VersionInfo<Long> parent;

    public DeviceLifeCycleStateInfo() {
    }

    public DeviceLifeCycleStateInfo(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, DeviceLifeCycle deviceLifeCycle, State state, MeteringTranslationService meteringTranslationService) {
        this.id = state.getId();
        this.isCustom = state.isCustom();
        this.isInitial = state.isInitial();
        this.version = state.getVersion();
        state.getStage().map(Stage::getName).ifPresent(stageName ->
                this.stage = new IdWithNameInfo(stageName, deviceLifeCycleConfigurationService.getStageDisplayName(EndDeviceStage.fromKey(stageName))));
        if (deviceLifeCycle != null) {
            this.parent = new VersionInfo<>(deviceLifeCycle.getId(), deviceLifeCycle.getVersion());
        }
        this.name = DefaultState.from(state).map(meteringTranslationService::getDisplayName).orElseGet(state::getName);
        addAllBusinessProcessInfos(onEntry, state.getOnEntryProcesses());
        addAllBusinessProcessInfos(onExit, state.getOnExitProcesses());
        addAllEndPointConfigurationInfos(onEntryEndPointConfigurations, state.getOnEntryEndPointConfigurations());
        addAllEndPointConfigurationInfos(onExitEndPointConfigurations, state.getOnExitEndPointConfigurations());
    }

    private void addAllBusinessProcessInfos(List<TransitionBusinessProcessInfo> target, List<ProcessReference> source) {
        source.stream()
                .map(ProcessReference::getStateChangeBusinessProcess)
                .map(x -> new TransitionBusinessProcessInfo(x.getId(), x.getProcessName(), x.getVersion()))
                .forEach(target::add);
    }

    private void addAllEndPointConfigurationInfos(List<TransitionEndPointConfigurationInfo> target, List<EndPointConfigurationReference> source) {
        source.stream()
                .map(EndPointConfigurationReference::getStateChangeEndPointConfiguration)
                .map(x -> new TransitionEndPointConfigurationInfo(x.getId(), x.getName(), x.getVersion()))
                .forEach(target::add);
    }
}