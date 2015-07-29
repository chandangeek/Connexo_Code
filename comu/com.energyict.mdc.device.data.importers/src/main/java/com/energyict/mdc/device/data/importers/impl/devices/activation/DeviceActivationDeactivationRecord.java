package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;

import java.time.Instant;
import java.util.Optional;

public class DeviceActivationDeactivationRecord extends DeviceTransitionRecord {

    private boolean activate;
    private Instant transitionActionDate;

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    @Override
    public Optional<Instant> getTransitionActionDate() {
        return Optional.ofNullable(transitionActionDate);
    }

    public void setTransitionActionDate(Instant transitionActionDate) {
        this.transitionActionDate = transitionActionDate;
    }
}
