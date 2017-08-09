package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.command.rest.impl.CommandInfo;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

/**
 * Created by bvn on 6/27/17.
 */
public class CommandInfoFactory {

    public CommandInfo from(DeviceMessageSpec deviceMessageSpec) {
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.category = deviceMessageSpec.getCategory().getName();
        commandInfo.command = deviceMessageSpec.getName();
        commandInfo.commandName = deviceMessageSpec.getId().name();
        return commandInfo;
    }
}
