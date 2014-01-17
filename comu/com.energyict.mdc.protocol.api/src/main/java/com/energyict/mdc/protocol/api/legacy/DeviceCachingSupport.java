package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;

/**
 * Provides functionality to allow to fetch and store certain cache objects a {@link DeviceProtocol}
 * can reuse over his communication sessions
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 14:23
 */
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
