package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

/**
 * Command to set the {@link DeviceProtocolCache} to a {@link DeviceProtocol} so it can be used
 * during communication with the Device.
 * <p>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 13:51
 */
public class DeviceProtocolSetCacheCommand extends SimpleComCommand {

    private final OfflineDevice device;

    public DeviceProtocolSetCacheCommand(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
        this.device = groupedDeviceCommand.getOfflineDevice();
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        deviceProtocol.setDeviceCache(this.device.getDeviceProtocolCache());
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND;
    }

    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

    @Override
    public String getDescriptionTitle() {
        return "Load the device protocol cache";
    }
}