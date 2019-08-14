/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;

/**
 * @author sva
 * @since 3/11/2014 - 10:56
 */
public class NoopCommandImpl extends SimpleComCommand {

    public NoopCommandImpl(GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        // Nothing to do
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.UNKNOWN;
    }

    @Override
    public String getDescriptionTitle() {
        return "No operations command";
    }
}