package com.energyict.mdc.engine.events;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;

/**
 * Marks a {@link ComServerEvent} as relating to a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (08:50)
 */
@ProviderType
public interface DeviceRelatedEvent extends ComServerEvent {

    /**
     * Gets the related {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
     *
     * @return The device
     */
    public Device getDevice ();

}