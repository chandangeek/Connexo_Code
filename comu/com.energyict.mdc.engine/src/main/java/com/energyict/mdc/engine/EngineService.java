package com.energyict.mdc.engine;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.cache.DeviceCache;

import java.io.Serializable;

/**
 * Provides services that relate to {@link com.energyict.mdc.device.data.Device}s.
 *
 * Copyrights EnergyICT
 * Date: 08/05/14
 * Time: 12:01
 */
public interface EngineService {

    public static String COMPONENTNAME = "CES";

    public DeviceCache newDeviceCache(Device device, Serializable simpleCacheObject);

    public DeviceCache findDeviceCacheByDeviceId(Device device);

}
