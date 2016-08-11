package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;

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
    public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        try {
            deviceProtocol.logOn();
        }catch(IllegalArgumentException e){
            throw new ConnectionSetupException(MessageSeeds.LOG_ON_FAILED, new ConnectionException(e));
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Log on to the device";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.LOGON;
    }

}