/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.cache;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;

public interface DeviceCache {

    void update();

    void delete();

    /**
     * Gets the id of the Device referring to this Cash
     *
     * @return the id of the Device
     */
    public long getDeviceId();

    /**
     * @return the cache object
     */
    public DeviceProtocolCache getSimpleCacheObject();

    /**
     * Sets the current cache object
     * @param deviceProtocolCache the current cacheObject
     */
    public void setCacheObject(DeviceProtocolCache deviceProtocolCache);

}