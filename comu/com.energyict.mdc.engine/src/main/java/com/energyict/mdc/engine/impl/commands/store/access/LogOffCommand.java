package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * A Disconnect command which performs a logical signOff with the device
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 11:13
 */
public class LogOffCommand extends SimpleComCommand {

    public LogOffCommand(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        deviceProtocol.logOff();
    }

    @Override
    public String getDescriptionTitle() {
        return "Log off from the device";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.LOGOFF;
    }

}
