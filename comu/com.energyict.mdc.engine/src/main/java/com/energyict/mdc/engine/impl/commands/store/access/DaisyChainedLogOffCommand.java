/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;

public class DaisyChainedLogOffCommand extends SimpleComCommand {

    public DaisyChainedLogOffCommand(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if (executionContext.hasBasicCheckFailed()) {
            deviceProtocol.logOff();
        } else {
            deviceProtocol.daisyChainedLogOff();
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Daisy chained log off";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.DAISY_CHAINED_LOGOFF;
    }

}