package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * A Daisy Chained Disconnect command
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 16:21
 */
public class DaisyChainedLogOffCommand extends SimpleComCommand {

    public DaisyChainedLogOffCommand(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        deviceProtocol.daisyChainedLogOff();
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.DAISY_CHAINED_LOGOFF;
    }

}