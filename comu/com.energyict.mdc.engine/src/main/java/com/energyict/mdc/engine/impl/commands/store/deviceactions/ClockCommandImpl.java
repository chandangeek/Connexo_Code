/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ClockTaskOptions;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;

import java.util.Optional;

/**
 * Implementation of a {@link ClockCommand}.<br>
 * Depending on the {@link com.energyict.mdc.tasks.ClockTaskType} a:
 * <ul>
 * <li>{@link SetClockCommandImpl}</li>
 * <li>{@link ForceClockCommandImpl}</li>
 * <li>{@link SynchronizeClockCommandImpl}</li>
 * </ul>
 * ... will be executed.
 *
 * @author gna
 * @since 8/05/12 - 14:55
 */
public class ClockCommandImpl extends CompositeComCommandImpl implements ClockCommand {

    private final ComTaskExecution comTaskExecution;

    /**
     * The used {@link TimeDifferenceCommand} (if used)
     */
    private TimeDifferenceCommand timeDifferenceCommand;

    /**
     * Indication of what ClockCommand will be performed for this action
     */
    private ComCommand clockCommand;

    private ClockTaskOptions clockTaskOptions;

    /**
     * @param clockTask        the ProtocolTask which models which actions must be performed
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     */
    public ClockCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final ClockTask clockTask, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        this.comTaskExecution = comTaskExecution;
        if (clockTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "clockTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (groupedDeviceCommand == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "groupedDeviceCommand", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.clockTaskOptions = new ClockTaskOptions(clockTask);
        updateCommand();
    }

    @Override
    public void updateAccordingTo(ClockTask clockTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        // Force clock overrules the other types - else leave the first type
        if (!clockTaskOptions.getClockTaskType().equals(ClockTaskType.FORCECLOCK)) {
            if (clockTask.getClockTaskType().equals(ClockTaskType.FORCECLOCK)) {
                // Overrule the clockTask with force clock
                this.clockTaskOptions = new ClockTaskOptions(clockTask);
                getCommands().values().remove(clockCommand);
                updateCommand();
            }
        }
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("clockTaskType").append(this.getClockTaskOptions().getClockTaskType().name());
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            if (!ClockTaskType.FORCECLOCK.equals(this.getClockTaskOptions().getClockTaskType())) {
                TimeDuration minDiffSeconds = new TimeDuration(this.getClockTaskOptions().getMinimumClockDifference().orElse(new TimeDuration(0)).getSeconds(), TimeDuration.TimeUnit.SECONDS);
                TimeDuration maxDiffSeconds = new TimeDuration(this.getClockTaskOptions().getMaximumClockDifference().orElse(new TimeDuration(0)).getSeconds(), TimeDuration.TimeUnit.SECONDS);

                if (ClockTaskType.SETCLOCK.equals(this.getClockTaskOptions().getClockTaskType())) {
                    builder.addProperty("minimumDifference").append(minDiffSeconds);
                    builder.addProperty("maximumDifference").append(maxDiffSeconds);
                } else if (ClockTaskType.SYNCHRONIZECLOCK.equals(this.getClockTaskOptions().getClockTaskType())) {
                    TimeDuration maxClockShiftSeconds = new TimeDuration(this.getClockTaskOptions().getMaximumClockShift().orElse(new TimeDuration(0)).getSeconds(), TimeDuration.TimeUnit.SECONDS);
                    builder.addProperty("minimumDifference").append(minDiffSeconds);
                    builder.addProperty("maximumDifference").append(maxDiffSeconds);
                    builder.addProperty("maximumClockShift").append(maxClockShiftSeconds);
                }
            }
            if (this.getTimeDifference().isPresent()) {
                TimeDuration diffInSeconds = new TimeDuration(this.getTimeDifferenceCommand().getTimeDifference().orElse(new TimeDuration(0)).getSeconds(), TimeDuration.TimeUnit.SECONDS);
                builder.addProperty("timeDifference").append(diffInSeconds);
            }
        }
    }

    /**
     * Initialize this {@link ComCommand}. The decorator pattern is used to make a distinction between which actions must be
     * performed for each {@link com.energyict.mdc.tasks.ClockTaskType}
     */
    private void updateCommand() {
        switch (clockTaskOptions.getClockTaskType()) {
            case SETCLOCK: {
                setClockCommand(getGroupedDeviceCommand().getSetClockCommand(this, this.comTaskExecution));
            }
            break;
            case FORCECLOCK: {
                setClockCommand(getGroupedDeviceCommand().getForceClockCommand(this, this.comTaskExecution));
            }
            break;
            case SYNCHRONIZECLOCK: {
                setClockCommand(getGroupedDeviceCommand().getSynchronizeClockCommand(this, this.comTaskExecution));
            }
            break;
            default: {
                // just for certainty, should never get here
                addIssue(getIssueService().newProblem(
                        getClockTaskOptions().getClockTaskType(),
                        MessageSeeds.UNKNOWN_CLOCKTASK_TYPE,
                        getClockTaskOptions().getClockTaskType()),
                        CompletionCode.UnexpectedError);
                setClockCommand(new UnKnownClockTaskTypeCommand(getGroupedDeviceCommand()));
            }
        }
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.CLOCK_COMMAND;
    }

    /**
     * Get the current ComCommand
     *
     * @return the current ComCommand
     */
    protected ComCommand getClockCommand() {
        return this.clockCommand;
    }

    private void setClockCommand(ComCommand clockCommand) {
        this.clockCommand = clockCommand;
    }

    @Override
    public TimeDifferenceCommand getTimeDifferenceCommand() {
        return this.timeDifferenceCommand;
    }

    @Override
    public void setTimeDifferenceCommand(final TimeDifferenceCommand timeDifferenceCommand) {
        this.timeDifferenceCommand = timeDifferenceCommand;
    }

    @Override
    public ClockTaskOptions getClockTaskOptions() {
        return clockTaskOptions;
    }

    @Override
    public Optional<TimeDuration> getTimeDifference() {
        if (getTimeDifferenceCommand() == null) {
            return Optional.empty();
        }
        return this.timeDifferenceCommand.getTimeDifference();
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed clock protocol task";
    }

    /**
     * ClockCommand which will do nothing
     */
    protected class UnKnownClockTaskTypeCommand extends SimpleComCommand {

        protected UnKnownClockTaskTypeCommand(final GroupedDeviceCommand groupedDeviceCommand) {
            super(groupedDeviceCommand);
        }

        @Override
        public ComCommandTypes getCommandType() {
            return null;
        }

        @Override
        public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
            // really nothing to do
        }

        protected LogLevel defaultJournalingLogLevel() {
            return LogLevel.TRACE;
        }

        @Override
        public String getDescriptionTitle() {
            return "Dummy clock command";
        }
    }

}