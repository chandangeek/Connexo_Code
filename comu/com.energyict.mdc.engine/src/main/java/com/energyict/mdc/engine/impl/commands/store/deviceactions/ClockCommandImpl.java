package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.ClockTask;

import com.elster.jupiter.time.TimeDuration;

import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.tasks.ClockTaskType.SETCLOCK;
import static com.energyict.mdc.tasks.ClockTaskType.SYNCHRONIZECLOCK;

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

    /**
     * The {@link ClockTask} which is used for modeling the actions
     */
    private final ClockTask clockTask;
    private final ComTaskExecution comTaskExecution;

    /**
     * The used {@link TimeDifferenceCommand} (if used)
     */
    private TimeDifferenceCommand timeDifferenceCommand;

    /**
     * Indication of what ClockCommand will be performed for this action
     */
    private ComCommand clockCommand;

    /**
     * @param clockTask the ProtocolTask which models which actions must be performed
     * @param comTaskExecution the ComTaskExecution that drives this ComCommand
     */
    public ClockCommandImpl(final ClockTask clockTask, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        this.comTaskExecution = comTaskExecution;
        if (clockTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "clockTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "timeDifferenceCommand", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.clockTask = clockTask;
        updateCommand();
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("clockTaskType").append(this.getClockTask().getClockTaskType().name());
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            if (SETCLOCK.equals(this.getClockTask().getClockTaskType())) {
                builder.addProperty("minimumDifference").append(this.getClockTask().getMinimumClockDifference().get());
                builder.addProperty("maximumDifference").append(this.getClockTask().getMaximumClockDifference().get());
            }
            else if (SYNCHRONIZECLOCK.equals(this.getClockTask().getClockTaskType())) {
                builder.addProperty("maximumClockShift").append(this.getClockTask().getMaximumClockShift().get());
            }
            if (this.getTimeDifferenceCommand() != null) {
                builder.addProperty("getTimeDifference").append(this.getTimeDifferenceCommand().getTimeDifference().map(TimeDuration::toString).orElse(""));
            }
        }
    }

    /**
     * Initialize this {@link ComCommand}. The decorator pattern is used to make a distinction between which actions must be
     * performed for each {@link com.energyict.mdc.tasks.ClockTaskType}
     */
    private void updateCommand() {
        switch (clockTask.getClockTaskType()) {
            case SETCLOCK: {
                setClockCommand(getCommandRoot().getSetClockCommand(this, this.comTaskExecution));
            }
            break;
            case FORCECLOCK: {
                setClockCommand(getCommandRoot().getForceClockCommand(this, this.comTaskExecution));
            }
            break;
            case SYNCHRONIZECLOCK: {
                setClockCommand(getCommandRoot().getSynchronizeClockCommand(this, this.comTaskExecution));
            }
            break;
            default: {
                // just for certainty, should never get here
                addIssue(getIssueService().newProblem(
                        clockTask.getClockTaskType(),
                        "unknownclocktasktype",
                        clockTask.getClockTaskType()),
                        CompletionCode.UnexpectedError);
                setClockCommand(new UnKnownClockTaskTypeCommand(getCommandRoot()));
            }
        }
    }

    @Override
    public List<Issue> getIssues() {
        return clockCommand.getIssues();
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.CLOCK_COMMAND;
    }

    private void setClockCommand(ComCommand clockCommand) {
        this.clockCommand = clockCommand;
    }

    /**
     * Get the current ComCommand
     *
     * @return the current ComCommand
     */
    protected ComCommand getClockCommand() {
        return this.clockCommand;
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
    public ClockTask getClockTask() {
        return this.clockTask;
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

        protected UnKnownClockTaskTypeCommand(final CommandRoot commandRoot) {
            super(commandRoot);
        }

        @Override
        public ComCommandTypes getCommandType() {
            return null;
        }

        @Override
        public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
            // really nothing to do
        }

        protected LogLevel defaultJournalingLogLevel () {
            return LogLevel.DEBUG;
        }

        @Override
        public String getDescriptionTitle() {
            return "Dummy clock command";
        }
    }

}