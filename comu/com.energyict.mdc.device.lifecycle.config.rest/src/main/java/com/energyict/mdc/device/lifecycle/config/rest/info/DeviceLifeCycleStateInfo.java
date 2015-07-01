package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleStateInfo {
    public long id;
    public String name;
    public boolean isCustom;
    public boolean isInitial;
    public long version;

    public DeviceLifeCycleStateInfo() {}

    public DeviceLifeCycleStateInfo(Thesaurus thesaurus, State state) {
        super();
        this.id = state.getId();
        this.isCustom = state.isCustom();
        this.isInitial = state.isInitial();
        this.version = state.getVersion();
        Optional<DefaultState> defaultState = DefaultState.from(state);
        if (defaultState.isPresent()){
            this.name = thesaurus.getStringBeyondComponent(defaultState.get().getKey(), defaultState.get().getKey()) ;
        } else {
            this.name = state.getName();
        }
    }
}
