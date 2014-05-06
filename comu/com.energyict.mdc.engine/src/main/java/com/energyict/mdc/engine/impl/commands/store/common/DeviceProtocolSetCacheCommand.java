package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.comserver.commands.core.SimpleComCommand;
import com.energyict.comserver.core.JobExecution;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

/**
 * Command to set the {@link DeviceProtocolCache} to a {@link DeviceProtocol} so it can be used
 * during communication with the Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 13:51
 */
public class DeviceProtocolSetCacheCommand extends SimpleComCommand {

    private final OfflineDevice device;

    public DeviceProtocolSetCacheCommand(final OfflineDevice device, final CommandRoot commandRoot) {
        super(commandRoot);
        this.device = device;
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
         deviceProtocol.setDeviceCache(this.device.getDeviceProtocolCache());
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND;
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

}