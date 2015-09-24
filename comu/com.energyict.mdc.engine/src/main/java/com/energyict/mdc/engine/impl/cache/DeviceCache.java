package com.energyict.mdc.engine.impl.cache;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;

import java.io.Serializable;

/**
 * Represents a simple object that holds a <i>cache</i> {@link Object} for a specific {@link com.energyict.mdc.protocol.api.device.BaseDevice}
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:30
 */
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