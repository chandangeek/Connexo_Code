package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

/**
 * Command to initialize a {@link DeviceProtocol}
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/07/12
 * Time: 14:11
 */
public class DeviceProtocolInitializeCommand extends SimpleComCommand {

    private final OfflineDevice device;
    private final ComChannelPlaceHolder comChannelPlaceHolder;

    public DeviceProtocolInitializeCommand(CommandRoot commandRoot, OfflineDevice device, ComChannelPlaceHolder comChannelPlaceHolder) {
        super(commandRoot);
        this.device = device;
        this.comChannelPlaceHolder = comChannelPlaceHolder;
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        deviceProtocol.init(device, comChannelPlaceHolder.getComChannel());
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE;
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

}