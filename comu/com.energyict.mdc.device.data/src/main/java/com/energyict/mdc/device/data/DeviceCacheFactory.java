package com.energyict.mdc.device.data;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;

import java.io.Serializable;

/**
 * Factory providing functionality to fetch the DeviceProtocolCache for a specific device from another bundle than mdc-all ...
 *
 * Copyrights EnergyICT
 * Date: 21/03/14
 * Time: 10:25
 */
public interface DeviceCacheFactory {

    DeviceProtocolCache findProtocolCacheByDeviceId(long deviceId);

    void removeDeviceCacheFor(long deviceId);

    DeviceProtocolCache createDeviceCacheFor(long deviceId, Serializable deviceCache);
}
