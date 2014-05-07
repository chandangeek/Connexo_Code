package com.energyict.mdc.engine.events;

import com.energyict.mdc.protocol.api.device.BaseDevice;

/**
 * Marks a {@link ComServerEvent} as relating to a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (08:50)
 */
public interface DeviceRelatedEvent extends ComServerEvent {

    /**
     * Gets the related {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
     *
     * @return The device
     */
    public BaseDevice getDevice ();

}