package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Models the event that occurs when data collection engine sets up a communication channel
 * with a master device and during the subsequent communication session,
 * the master device reports one or more slave devices that are not known.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (15:00)
 * @see com.energyict.mdc.engine.impl.EventType#UNKNOWN_SLAVE_DEVICE
 */
public class UnknownSlaveDeviceEvent {

    private final String masterDeviceId;
    private final String deviceIdentifier;

    public UnknownSlaveDeviceEvent(DeviceIdentifier masterDeviceIdentifier, DeviceIdentifier deviceIdentifier) {
        super();
        this.masterDeviceId = masterDeviceIdentifier.toString();
        this.deviceIdentifier = deviceIdentifier.toString();
    }

    public String getMasterDeviceId() {
        return masterDeviceId;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

}