package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.VerifyTimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.tasks.history.CompletionCode;

/**
 * Proper implementation for a {@link VerifyTimeDifferenceCommand}
 *
 * @author sva
 * @since 18/04/13 - 10:01
 */
public class VerifyTimeDifferenceCommandImpl extends SimpleComCommand implements VerifyTimeDifferenceCommand {

    private final BasicCheckCommand basicCheckCommand;
    /**
     * The difference in time between the Collection Software and the Meter
     */
    private TimeDuration timeDifference;

    /**
     * The maximum allowed difference in time between the Collection Software and the Meter
     */
    private TimeDuration maximumClockDifference;

    public VerifyTimeDifferenceCommandImpl(BasicCheckCommand basicCheckCommand, final CommandRoot commandRoot) {
        super(commandRoot);
        this.basicCheckCommand = basicCheckCommand;
        maximumClockDifference = basicCheckCommand.getBasicCheckTask().getMaximumClockDifference();
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("maximumDifference").append(this.maximumClockDifference);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.timeDifference = this.basicCheckCommand.getTimeDifference();

        if (Math.abs(this.timeDifference.getMilliSeconds()) > this.maximumClockDifference.getMilliSeconds()) {
            addIssue(getIssueService().newProblem(
                    getCommandType(),
                    Environment.DEFAULT.get().getTranslation("CSC-CONF-134").replaceAll("'", "''"),
                    this.timeDifference,
                    this.maximumClockDifference),
                    CompletionCode.TimeError);
            throw DeviceConfigurationException.timeDifferenceExceeded(this.timeDifference.getMilliSeconds(), this.maximumClockDifference.getMilliSeconds());
        }
    }

    public TimeDuration getTimeDifference() {
        if (this.timeDifference == null) {
            return VerifyTimeDifferenceCommand.DID_NOT_READ_TIME_DIFFERENCE;
        }
        return timeDifference;
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.VERIFY_TIME_DIFFERENCE_COMMAND;
    }

}