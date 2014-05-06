package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * A LogOn command performs a logical signOn with the device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 11:12
 */
public class LogOnCommand extends SimpleComCommand {

    public LogOnCommand(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        deviceProtocol.logOn();
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.LOGON;
    }

}