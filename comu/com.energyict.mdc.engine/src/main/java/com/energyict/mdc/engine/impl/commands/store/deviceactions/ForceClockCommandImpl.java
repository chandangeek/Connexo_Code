/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.ForceClockCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Command to force the device time to the current system time
 */
public class ForceClockCommandImpl extends SimpleComCommand implements ForceClockCommand {

    private Date timeSet;

    public ForceClockCommandImpl(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
    }

    public void doExecute (final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        Date now = Date.from(getCommandRoot().getServiceProvider().clock().instant());
        deviceProtocol.setTime(now);
        this.timeSet = now;
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.FORCE_CLOCK_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Force set the device time";
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            builder.addLabel(MessageFormat.format("Time was forcefully set to {0}", this.timeSet));
        }
    }

}