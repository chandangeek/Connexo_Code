package com.energyict.mdc.device.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.HasId;

public class DeviceState implements HasId {
    private State state;


    public DeviceState(State state) {
        this.state = state;
    }

    @Override
    public long getId() {
        return state.getId();
    }
}
