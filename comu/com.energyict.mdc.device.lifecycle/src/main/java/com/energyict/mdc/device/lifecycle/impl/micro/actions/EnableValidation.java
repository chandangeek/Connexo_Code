package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will enable validation on the Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-20 (09:29)
 */
public class EnableValidation implements ServerMicroAction {

    private final Instant lastChecked;

    public EnableValidation(Instant lastChecked) {
        super();
        this.lastChecked = lastChecked;
    }

    @Override
    public void execute(Device device) {
        device.forValidation().activateValidation(this.lastChecked);
    }

}