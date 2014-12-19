package com.energyict.mdc.engine.impl.commands.store.common;


import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

/**
 * Updates and 'holds' the {@link DeviceProtocolCache} of a {@link DeviceProtocol}.
 * Command will/must be run at the end of the communication so the {@link DeviceProtocol} has time to update the update during communication.
 * <p>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:07
 */
public class DeviceProtocolUpdateCacheCommand extends SimpleComCommand {

    private final OfflineDevice device;

    public DeviceProtocolUpdateCacheCommand(final OfflineDevice device, final CommandRoot commandRoot) {
        super(commandRoot);
        this.device = device;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(device.getDeviceIdentifier());
        updatedDeviceCache.setCollectedDeviceCache(deviceProtocol.getDeviceCache());
        addCollectedDataItem(updatedDeviceCache);
    }

    private DeviceService getDeviceDataService() {
        return getCommandRoot().getServiceProvider().deviceService();
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND;
    }

    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

}