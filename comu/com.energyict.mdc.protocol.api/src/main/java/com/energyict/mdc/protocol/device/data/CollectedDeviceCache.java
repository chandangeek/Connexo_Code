package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;

/**
 * A CollectedDeviceCache identifies the {@link com.energyict.mdc.protocol.DeviceProtocolCache}
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
