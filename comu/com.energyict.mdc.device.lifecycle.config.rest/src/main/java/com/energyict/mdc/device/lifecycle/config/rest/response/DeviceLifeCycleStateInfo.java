package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleStateInfo extends IdWithNameInfo {
    public DeviceLifeCycleStateInfo() {}

    public DeviceLifeCycleStateInfo(Thesaurus thesaurus, State state) {
        super();
        this.id = state.getId();
        Optional<DefaultState> defaultState = DefaultState.from(state);
        if (defaultState.isPresent()){
            this.name = thesaurus.getStringBeyondComponent(defaultState.get().getKey(), defaultState.get().getDefaultFormat()) ;
        } else {
            this.name = state.getName();
        }
    }
}
