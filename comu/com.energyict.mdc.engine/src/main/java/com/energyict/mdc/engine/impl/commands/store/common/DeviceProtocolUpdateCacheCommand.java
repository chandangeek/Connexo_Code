package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.comserver.commands.core.SimpleComCommand;
import com.energyict.comserver.core.JobExecution;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;

/**
 * Updates and 'holds' the {@link DeviceProtocolCache} of a {@link DeviceProtocol}.
 * Command will/must be run at the end of the communication so the {@link DeviceProtocol} has time to update the update during communication.
 * <p/>
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
    public void doExecute (DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(new DeviceIdentifierById(device.getId()));
        updatedDeviceCache.setDeviceCache(deviceProtocol.getDeviceCache());
        addCollectedDataItem(updatedDeviceCache);
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND;
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

}