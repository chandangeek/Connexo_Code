/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.SetClockCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Command to set the device time on the current system time <b>if and only if</b> the timeDifference is between
 * the Minimum and Maximum defined times. Otherwise a warning will be added to the issueList.
 */
public class SetClockCommandImpl extends SimpleComCommand implements SetClockCommand {

    /**
     * The used {@link ClockCommand}, owner of this SetClockCommand
     */
    private ClockCommand clockCommand;
    private Date timeSet;

    public SetClockCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final ClockCommand clockCommand, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        this.clockCommand = clockCommand;
        this.clockCommand.setTimeDifferenceCommand(getGroupedDeviceCommand().getTimeDifferenceCommand(clockCommand, comTaskExecution));
    }

    /**
     * Perform the actions which are owned by this ComCommand
     */
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        long timeDifference = getTimeDifference().orElse(TimeDuration.TimeUnit.MILLISECONDS.during(0)).getMilliSeconds();
        if (aboveMaximum(timeDifference)) {
            addIssue(getIssueService().newWarning(timeDifference,MessageSeeds.TIME_DIFFERENCE_LARGER_THAN_MAX_DEFINED, timeDifference), CompletionCode.ConfigurationWarning);
        } else if (!belowMinimum(timeDifference)) {
            Date now = Date.from(getCommandRoot().getServiceProvider().clock().instant());
            deviceProtocol.setTime(Date.from(getCommandRoot().getServiceProvider().clock().instant()));
            this.timeSet = now;
        }
    }

    private boolean aboveMaximum(final long timeDifference) {
        return getMaxDiff() < Math.abs(timeDifference);
    }

    private Optional<TimeDuration> getTimeDifference() {
        return this.clockCommand.getTimeDifference();
    }

    private long getMaxDiff() {
        return clockCommand.getClockTaskOptions().getMaximumClockDifference().map(TimeDuration::getMilliSeconds).orElse(0L);
    }

    private Optional<TimeDuration> getMaxClockDifference() {
        return clockCommand.getClockTaskOptions().getMaximumClockDifference();
    }

    private boolean belowMinimum(final long timeDifference) {
        return getMinDiff() > Math.abs(timeDifference);
    }

    private long getMinDiff() {
        return clockCommand.getClockTaskOptions().getMinimumClockDifference().map(TimeDuration::getMilliSeconds).orElse(0L);
    }

    private Optional<TimeDuration> getMinimumClockDifference() {
        return clockCommand.getClockTaskOptions().getMinimumClockDifference();
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            TimeDuration minDiffSeconds = new TimeDuration(this.getMinimumClockDifference().orElse(new TimeDuration(0)).getSeconds(), TimeDuration.TimeUnit.SECONDS);
            TimeDuration maxDiffSeconds = new TimeDuration(this.getMaxClockDifference().orElse(new TimeDuration(0)).getSeconds(), TimeDuration.TimeUnit.SECONDS);
            builder.addProperty("minimumDifference").append(minDiffSeconds);
            builder.addProperty("maximumDifference").append(maxDiffSeconds);
            if (getTimeDifference().isPresent()) {
                builder.addProperty("timeDifference").append(this.getTimeDifference().get());
            }
        }
        if (this.timeSet != null) {
            builder.addLabel(MessageFormat.format("Time was successfully set to {0}", this.timeSet));
        }
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.SET_CLOCK_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Set the device time";
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }
}