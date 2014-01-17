package com.energyict.mdc.protocol.api.inbound;

import com.energyict.mdc.protocol.api.device.Device;

import java.io.Serializable;

/**
 * Identifies a device that started inbound communication.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (16:51)
 */
public interface DeviceIdentifier extends Serializable {

    public String getIdentifier();

    /**
     * Finds the {@link Device} that is uniquely identified by this DeviceIdentifier.
     *
     * @return The Device
     */
    public Device findDevice ();

}