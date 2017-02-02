package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.*;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.tasks.BasicCheckTask;

import java.util.Optional;

/**
 * Provides a proper implementation for the {@link BasicCheckCommand}
 *
 * @author gna
 * @since 31/05/12 - 13:17
 */
public class BasicCheckCommandImpl extends CompositeComCommandImpl implements BasicCheckCommand {

    private Optional<TimeDuration> maximumClockDifference;

    /**
     * The used {@link TimeDifferenceCommand}
     */
    private TimeDifferenceCommand timeDifferenceCommand;

    /**
     * The used {@link VerifyTimeDifferenceCommand}
     */
    private VerifyTimeDifferenceCommand verifyTimeDifferenceCommand;

    /**
     * The used {@link VerifySerialNumberCommand}
     */
    private VerifySerialNumberCommand verifySerialNumberCommand;

    public BasicCheckCommandImpl(final BasicCheckTask basicCheckTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        if (basicCheckTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "basicCheckTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }

        if (basicCheckTask.verifyClockDifference()) {
            this.maximumClockDifference = basicCheckTask.getMaximumClockDifference();
            this.timeDifferenceCommand = groupedDeviceCommand.getTimeDifferenceCommand(this, comTaskExecution);
            this.verifyTimeDifferenceCommand = groupedDeviceCommand.getVerifyTimeDifferenceCommand(this, comTaskExecution);
        }
        if (basicCheckTask.verifySerialNumber()) {
            this.verifySerialNumberCommand = groupedDeviceCommand.getVerifySerialNumberCommand(this, comTaskExecution);
        }
    }

    @Override
    public void updateAccordingTo(BasicCheckTask basicCheckTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (basicCheckTask.verifySerialNumber() && this.verifySerialNumberCommand == null) {
            this.verifySerialNumberCommand = groupedDeviceCommand.getVerifySerialNumberCommand(this, comTaskExecution);
        }

        if (basicCheckTask.verifyClockDifference()) {
            if (this.verifyTimeDifferenceCommand == null) { // If not existing, then add it
                this.maximumClockDifference = basicCheckTask.getMaximumClockDifference();
                this.timeDifferenceCommand = groupedDeviceCommand.getTimeDifferenceCommand(this, comTaskExecution);
                this.verifyTimeDifferenceCommand = groupedDeviceCommand.getVerifyTimeDifferenceCommand(this, comTaskExecution);
            } else { // Else update the existing one
                if (basicCheckTask.getMaximumClockDifference().isPresent()) {
                    if (!this.getMaximumClockDifference().isPresent() || basicCheckTask.getMaximumClockDifference().get().getMilliSeconds() < this.getMaximumClockDifference().get().getMilliSeconds()) {
                        this.maximumClockDifference = basicCheckTask.getMaximumClockDifference();
                    }
                }
            }
        }
    }

    /**
     * @return the ComCommandType of this command
     */
    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.BASIC_CHECK_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed basic check protocol task";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.verifySerialNumberCommand != null) {
            builder.addLabel("check serial number");
        }

        if (this.verifyTimeDifferenceCommand != null) {
            builder.addLabel("check maximum clock difference");
        }
    }

    @Override
    public Optional<TimeDuration> getMaximumClockDifference() {
        return maximumClockDifference;
    }

    @Override
    public TimeDifferenceCommand getTimeDifferenceCommand() {
        return timeDifferenceCommand;
    }

    @Override
    public VerifyTimeDifferenceCommand getVerifyTimeDifferenceCommand() {
        return verifyTimeDifferenceCommand;
    }

    @Override
    public VerifySerialNumberCommand getVerifySerialNumberCommand() {
        return verifySerialNumberCommand;
    }

    /**
     * Get the TimeDifference of the BasicCheckCommand. If the timeDifference is not read,
     * then TimeDifferenceCommand#DID_NOT_READ_TIME_DIFFERENCE will be returned.
     *
     * @return the timeDifference
     */
    @Override
    public Optional<TimeDuration> getTimeDifference() {
        return getTimeDifferenceCommand().getTimeDifference();
    }
}