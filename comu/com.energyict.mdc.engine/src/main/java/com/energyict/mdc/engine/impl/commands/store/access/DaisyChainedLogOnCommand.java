package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * A Daisy chained SignOn command
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 16:23
 */
public class DaisyChainedLogOnCommand extends SimpleComCommand {

    public DaisyChainedLogOnCommand(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        deviceProtocol.daisyChainedLogOn();
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.DAISY_CHAINED_LOGON;
    }

}