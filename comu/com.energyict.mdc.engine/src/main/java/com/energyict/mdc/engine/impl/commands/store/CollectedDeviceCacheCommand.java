package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides functionality to update the cache of a Device in the database
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:22
 */
public class CollectedDeviceCacheCommand extends DeviceCommandImpl {

    private final UpdatedDeviceCache deviceCache;

    public CollectedDeviceCacheCommand(UpdatedDeviceCache deviceCache) {
        super();
        this.deviceCache = deviceCache;
    }

    @Override
    public void doExecute (ComServerDAO comServerDAO) {
        // we will only perform the update when the cache actually changed
        DeviceProtocolCache collectedDeviceCache = this.deviceCache.getCollectedDeviceCache();
        if (collectedDeviceCache != null && collectedDeviceCache.contentChanged()) {
            DeviceIdentifier<Device> deviceIdentifier = this.deviceCache.getDeviceIdentifier();
            Device device = deviceIdentifier.findDevice();
            DeviceCache deviceCache = getEngineService().findDeviceCacheByDeviceId(device);
            if(deviceCache != null){
                deviceCache.setCacheObject(collectedDeviceCache);
                deviceCache.update();
            } else {
                deviceCache = getEngineService().newDeviceCache(device, collectedDeviceCache);
                deviceCache.save();
            }
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        builder.addProperty("deviceIdentifier").append(this.deviceCache.getDeviceIdentifier());
    }

}