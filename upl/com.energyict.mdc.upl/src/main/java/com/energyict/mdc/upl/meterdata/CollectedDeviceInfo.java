package com.energyict.mdc.upl.meterdata;


import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * CollectedDeviceInfo is a type of {@link CollectedData} that can be used to keep track of additional device information collected for a specific device<br>
 * E.g. to keep track of the received IP address / nodeAddress / ... of a specific device.
 *
 * @author sva
 * @since 16/10/2014 - 16:02
 */
public interface CollectedDeviceInfo extends CollectedData {

    /**
     * @return the unique identifier of the Device for which the additional info is collected
     */
    DeviceIdentifier getDeviceIdentifier();

}