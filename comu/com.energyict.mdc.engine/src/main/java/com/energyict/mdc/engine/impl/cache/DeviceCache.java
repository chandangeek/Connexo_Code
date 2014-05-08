package com.energyict.mdc.engine.impl.cache;

import java.io.Serializable;

/**
 * Represents a simple object that holds a <i>cache</i> {@link Object} for a specific {@link com.energyict.mdc.protocol.api.device.BaseDevice}
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:30
 */
public interface DeviceCache {

    void save();

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
    public Serializable getSimpleCacheObject();

    /**
     * Sets the current cache object
     * @param cacheObject the current cacheObject
     */
    public void setCacheObject(Serializable cacheObject);

}