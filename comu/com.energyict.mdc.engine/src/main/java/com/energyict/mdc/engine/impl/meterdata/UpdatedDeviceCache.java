package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.CollectedDeviceCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceCache;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * Implementation of a DeviceProtocolCache collected during communication with a Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:14
 */
public class UpdatedDeviceCache extends CollectedDeviceData implements CollectedDeviceCache {

    /**
     * Unique identification object of who needs to update his cache.
     */
    private final DeviceIdentifier<?> deviceIdentifier;

    /**
     * The DeviceProtocolCache that the protocol used/updated during communication.
     */
    private DeviceProtocolCache updatedDeviceProtocolCache;

    /**
     * Default constructor.
     *
     * @param deviceIdentifier unique identification of the device which need s to update his cache
     */
    public UpdatedDeviceCache(DeviceIdentifier<?> deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return false;
    }

    @Override
    public DeviceProtocolCache getCollectedDeviceCache() {
        return updatedDeviceProtocolCache;
    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return this.deviceIdentifier;
    }

    /**
     * Sets the {@link DeviceProtocolCache}.
     *
     * @param deviceProtocolCache The DeviceProtocolCache
     */
    public void setCollectedDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.updatedDeviceProtocolCache = deviceProtocolCache;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedDeviceCacheCommand(this, this.getComTaskExecution(), serviceProvider);
    }

}