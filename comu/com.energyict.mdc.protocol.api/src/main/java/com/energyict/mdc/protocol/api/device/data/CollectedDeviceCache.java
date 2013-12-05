package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * A CollectedDeviceCache identifies the {@link DeviceProtocolCache}
 * that the protocol used/updated during a communication session with a device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:12
 */
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
    public DeviceIdentifier getDeviceIdentifier();

}
