package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import javax.inject.Inject;
import java.util.Objects;

public class DeviceLifeCycleStateFactory {

    private final Thesaurus thesaurus;

    @Inject
    public DeviceLifeCycleStateFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceLifeCycleStateInfo from(DeviceLifeCycle deviceLifeCycle, State state) {
        Objects.requireNonNull(deviceLifeCycle);
        Objects.requireNonNull(state);
        return new DeviceLifeCycleStateInfo(thesaurus, deviceLifeCycle, state);
    }
}
