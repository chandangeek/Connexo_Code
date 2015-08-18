package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import java.util.Date;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.SetClockCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

/**
 * Command to set the device time on the current system time <b>if and only if</b> the timeDifference is between
 * the Minimum and Maximum defined times. Otherwise a warning will be added to the issueList.
 */
public class SetClockCommandImpl extends SimpleComCommand implements SetClockCommand {

    /**
     * The used {@link ClockCommand}, owner of this SetClockCommand
     */
    private ClockCommand clockCommand;

    public SetClockCommandImpl(final ClockCommand clockCommand, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        this.clockCommand = clockCommand;
        this.clockCommand.setTimeDifferenceCommand(getCommandRoot().getTimeDifferenceCommand(clockCommand, comTaskExecution));
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("minimumDifference").append(this.getMinDiff()).append("ms");
        builder.addProperty("maximumDifference").append(this.getMaxDiff()).append("ms");
    }

    /**
     * Perform the actions which are owned by this ComCommand
     */
    public void doExecute (final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        long timeDifference = this.clockCommand.getTimeDifference().orElse(TimeDuration.TimeUnit.MILLISECONDS.during(0)).getMilliSeconds();
        if (aboveMaximum(timeDifference)) {
            addIssue(getIssueService().newWarning(timeDifference, "timediffXlargerthanmaxdefined", timeDifference), CompletionCode.ConfigurationWarning);
        } else if (!belowMinimum(timeDifference)) {
            deviceProtocol.setTime(Date.from(getCommandRoot().getServiceProvider().clock().instant()));
        }
    }

    private boolean aboveMaximum(final long timeDifference) {
        return getMaxDiff() < Math.abs(timeDifference);
    }

    private long getMaxDiff() {
        return clockCommand.getClockTask().getMaximumClockDifference().map(TimeDuration::getMilliSeconds).orElse(0L);
    }

    private boolean belowMinimum(final long timeDifference) {
        return getMinDiff() > Math.abs(timeDifference);
    }

    private long getMinDiff() {
        return clockCommand.getClockTask().getMinimumClockDifference().map(TimeDuration::getMilliSeconds).orElse(0L);
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.SET_CLOCK_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Set the device time";
    }

    @Override
    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.INFO;
    }

}