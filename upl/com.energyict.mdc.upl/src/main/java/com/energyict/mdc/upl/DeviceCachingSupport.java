package com.energyict.mdc.upl;

import com.energyict.mdc.upl.cache.DeviceProtocolCache;

/**
 * Provides functionality to allow to fetch and store certain cache objects a {@link DeviceProtocol}
 * can reuse across different communication sessions.
 * <p/>
 *
 * Date: 31/08/12
 * Time: 14:23
 */
public interface DeviceCachingSupport {

    /**
     * Sets the {@link DeviceProtocolCache} fetched from the Database
     *
     * @param deviceProtocolCache the DeviceProtocolCache for this Device
     */
    void setDeviceCache(DeviceProtocolCache deviceProtocolCache);

    /**
     * Gets the <i>updated </i>{@link DeviceProtocolCache}
     *
     * @return the deviceCache
     */
    DeviceProtocolCache getDeviceCache();

}