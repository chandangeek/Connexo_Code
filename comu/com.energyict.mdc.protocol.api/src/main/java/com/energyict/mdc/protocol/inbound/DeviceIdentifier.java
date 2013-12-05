package com.energyict.mdc.protocol.inbound;

import java.io.Serializable;

/**
 * Identifies a device that started inbound communication.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (16:51)
 */
public interface DeviceIdentifier extends Serializable {

    public String getIdentifier();

}