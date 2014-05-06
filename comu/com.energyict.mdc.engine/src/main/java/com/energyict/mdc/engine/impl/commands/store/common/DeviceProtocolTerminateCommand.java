package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.comserver.commands.core.SimpleComCommand;
import com.energyict.comserver.core.JobExecution;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * Command to terminate a session with a {@link DeviceProtocol}
 *
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 8:31
 */
public class DeviceProtocolTerminateCommand extends SimpleComCommand {

    public DeviceProtocolTerminateCommand(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        deviceProtocol.terminate();
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_TERMINATE;
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

}