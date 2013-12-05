package com.energyict.mdc.protocol.tasks.support;

import com.energyict.mdc.protocol.DeviceProtocolCache;

/**
 * Provides functionality to allow to fetch and store certain cache objects a {@link com.energyict.mdc.protocol.DeviceProtocol}
 * can reuse over his communication sessions
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 14:23
 */
public interface DeviceCachingSupport {

    /**
     * Sets the {@link com.energyict.mdc.protocol.DeviceProtocolCache} fetched from the Database
     *
     * @param deviceProtocolCache the DeviceProtocolCache for this Device
     */
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache);

    /**
     * Gets the <i>updated </i>{@link com.energyict.mdc.protocol.DeviceProtocolCache}
     *
     * @return the deviceCache
     */
    public DeviceProtocolCache getDeviceCache();

}
