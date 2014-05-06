package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifySerialNumberCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyTimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.tasks.BasicCheckTask;

/**
 * Provides a proper implementation for the {@link BasicCheckCommand}
 *
 * @author gna
 * @since 31/05/12 - 13:17
 */
public class BasicCheckCommandImpl extends CompositeComCommandImpl implements BasicCheckCommand {

    /**
     * The {@link BasicCheckTask} which is used for modeling the actions in this command
     */
    private final BasicCheckTask basicCheckTask;

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

    public BasicCheckCommandImpl(final BasicCheckTask basicCheckTask, CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        if (basicCheckTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "basicCheckTask");
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot");
        }
        this.basicCheckTask = basicCheckTask;

        if (this.basicCheckTask.verifyClockDifference()) {
            this.timeDifferenceCommand = commandRoot.getTimeDifferenceCommand(this, comTaskExecution);
            this.verifyTimeDifferenceCommand = commandRoot.getVerifyTimeDifferenceCommand(this, this, comTaskExecution);
        }
        if (this.basicCheckTask.verifySerialNumber()) {
            this.verifySerialNumberCommand = commandRoot.getVerifySerialNumberCommand(this, comTaskExecution);
        }
    }

    /**
     * @return the ComCommandType of this command
     */
    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.BASIC_CHECK_COMMAND;
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("readClockDifference").append(this.basicCheckTask.verifyClockDifference());
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG) && this.basicCheckTask.getMaximumClockDifference() != null) {
            builder.addProperty("readClockDifferenceMaximum(s)").append(this.basicCheckTask.getMaximumClockDifference().getSeconds());
        }
        if (this.basicCheckTask.verifySerialNumber()) {
            builder.addLabel("check serial number");
        }
        if (   this.verifyTimeDifferenceCommand!= null
            && this.verifyTimeDifferenceCommand.getTimeDifference() != null
            && !this.verifyTimeDifferenceCommand.getTimeDifference().isEmpty()) {
            builder.addProperty("getTimeDifference").append(this.verifyTimeDifferenceCommand.getTimeDifference());
        }
    }

    @Override
    public BasicCheckTask getBasicCheckTask() {
        return basicCheckTask;
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
    public TimeDuration getTimeDifference() {
        return getTimeDifferenceCommand().getTimeDifference();
    }

}