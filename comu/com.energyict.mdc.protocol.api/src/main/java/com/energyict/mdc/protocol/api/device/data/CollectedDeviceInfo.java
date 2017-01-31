/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;


import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

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
    public DeviceIdentifier getDeviceIdentifier();

    public void setDataCollectionConfiguration (DataCollectionConfiguration configuration);

}
