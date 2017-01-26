package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * An event indicating the device, uniquely identified by its {@link DeviceIdentifier},
 * has no {@link com.energyict.mdc.upl.meterdata.LogBook LogBooks} configured.
 *
 * @author sva
 * @since 14/12/12 - 9:18
 */

public class NoLogBooksForDeviceEvent {

    /**
     * The unique deviceIdentifier identifying the device which has no {@link com.energyict.mdc.upl.meterdata.LogBook LogBooks} configured.
     */
    private String deviceIdentifier;

    public NoLogBooksForDeviceEvent(DeviceIdentifier deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier.toString();
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

}