/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

public interface CollectedDeviceCache extends CollectedData {

    /**
     * Gets the cache object from this Communication session
     *
     * @return the DeviceProtocolCache
     */
    public DeviceProtocolCache getCollectedDeviceCache();

    /**
     * @return the unique identifier of the Device
     */
    public DeviceIdentifier<?> getDeviceIdentifier();

}
