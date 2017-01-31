/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;

public interface DeviceCachingSupport {

    /**
     * Sets the {@link DeviceProtocolCache} fetched from the Database
     *
     * @param deviceProtocolCache the DeviceProtocolCache for this Device
     */
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache);

    /**
     * Gets the <i>updated </i>{@link DeviceProtocolCache}
     *
     * @return the deviceCache
     */
    public DeviceProtocolCache getDeviceCache();

}
