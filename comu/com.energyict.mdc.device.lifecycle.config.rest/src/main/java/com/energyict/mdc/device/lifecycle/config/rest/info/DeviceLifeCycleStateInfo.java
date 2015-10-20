package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;
import java.util.stream.Collectors;

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

    public DeviceLifeCycleStateInfo(Thesaurus thesaurus, DeviceLifeCycle deviceLifeCycle, State state) {
        super();
        this.id = state.getId();
        this.isCustom = state.isCustom();
        this.isInitial = state.isInitial();
        this.version = state.getVersion();
        Optional<DefaultState> defaultState = DefaultState.from(state);
        if (defaultState.isPresent()){
            this.name = thesaurus.getString(defaultState.get().getKey(), null);
            if (this.name == null) {
                this.name = thesaurus.getStringBeyondComponent(defaultState.get().getKey(), defaultState.get().getKey());
            }
        } else {
            this.name = state.getName();
        }
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
