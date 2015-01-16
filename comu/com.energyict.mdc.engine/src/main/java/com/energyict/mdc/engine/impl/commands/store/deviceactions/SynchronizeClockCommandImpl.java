package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.SynchronizeClockCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Command to synchronize the clock based on a maximum clock shift.
 */
public class SynchronizeClockCommandImpl extends SimpleComCommand implements SynchronizeClockCommand {

    /**
     * The used {@link ClockCommand}
     */
    private ClockCommand clockCommand;
    private Date timeSet;

    public SynchronizeClockCommandImpl(final ClockCommand clockCommand, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        this.clockCommand = clockCommand;
        this.clockCommand.setTimeDifferenceCommand(getCommandRoot().getTimeDifferenceCommand(clockCommand, comTaskExecution));
    }

    /**
     * @return the ComCommandType of this command
     */
    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Synchronize the device time";
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("maximumClockShift").append(this.clockCommand.getClockTask().getMaximumClockShift().map(TimeDuration::toString).orElse(""));
        if (this.timeSet != null && this.isJournalingLevelEnabled(serverLogLevel, LogLevel.INFO)) {
            builder.addLabel(MessageFormat.format("Time was forcefully set to {0}", this.timeSet));
        }
    }

    /**
     * Perform the actions which are owned by this ComCommand
     */
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        long timeDifference = clockCommand.getTimeDifference().orElse(TimeDuration.TimeUnit.MILLISECONDS.during(0)).getMilliSeconds();
        if (Math.abs(timeDifference) <= clockCommand.getClockTask().getMaximumClockDifference().map(TimeDuration::getMilliSeconds).orElse(0L)){
            long timeShift = getTimeShift(timeDifference);
            if (timeShift != 0) {
                long currentDeviceTime = getCommandRoot().getServiceProvider().clock().millis() - timeDifference;
                Date now = new Date(currentDeviceTime + timeShift);
                deviceProtocol.setTime(now);
                this.timeSet = now;
            } else {
                addIssue(getIssueService().newWarning(timeDifference, "timediffXbelowthanmindefined", timeDifference), CompletionCode.ConfigurationWarning);
            }
        } else {
            addIssue(getIssueService().newWarning(timeDifference, "timediffXlargerthanmaxdefined", timeDifference), CompletionCode.ConfigurationWarning);
        }
    }

    /**
     * We check if the timeDifference is between the {@link com.energyict.mdc.tasks.ClockTask#getMinimumClockDifference()}
     * and {@link com.energyict.mdc.tasks.ClockTask#getMaximumClockShift()}, and calculate the clockShift for this action
     *
     * @param timeDifference the current TimeDifference
     * @return the calculated clockShift in MilliSeconds
     */
    private long getTimeShift(final long timeDifference) {
        Long maxClockShiftMillis = clockCommand.getClockTask().getMaximumClockShift().map(TimeDuration::getMilliSeconds).orElse(0L);
        if (maxClockShiftMillis <= Math.abs(timeDifference)) {
            return (long) (maxClockShiftMillis * Math.signum(timeDifference));
        } else if (clockCommand.getClockTask().getMinimumClockDifference().map(TimeDuration::getMilliSeconds).orElse(0L) <= Math.abs(timeDifference)) {
            return timeDifference;
        } else {
            return 0L;
        }
    }

}