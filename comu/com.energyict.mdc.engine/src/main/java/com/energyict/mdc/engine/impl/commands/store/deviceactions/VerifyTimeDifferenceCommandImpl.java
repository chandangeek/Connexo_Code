package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.VerifyTimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;

import com.elster.jupiter.time.TimeDuration;

import java.util.Optional;

/**
 * Proper implementation for a {@link VerifyTimeDifferenceCommand}.
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
        maximumClockDifference = basicCheckCommand.getBasicCheckTask().getMaximumClockDifference().orElseGet(() -> TimeDuration.millis(0));
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("maximumDifference").append(this.maximumClockDifference);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.timeDifference = this.basicCheckCommand.getTimeDifference().orElse(TimeDuration.TimeUnit.MILLISECONDS.during(0));

        if (Math.abs(this.timeDifference.getMilliSeconds()) > this.maximumClockDifference.getMilliSeconds()) {
            addIssue(getIssueService().newProblem(
                    getCommandType(),
// Todo: Add to MessageSeeds
                    "Time difference exceeds the configured maximum\\: The time difference ({0}) is larger than the configured allowed maximum ({1})",
                    this.timeDifference,
                    this.maximumClockDifference),
                    CompletionCode.TimeError);
            throw DeviceConfigurationException.timeDifferenceExceeded(MessageSeeds.MAXIMUM_TIME_DIFFERENCE_EXCEEDED, this.timeDifference.getMilliSeconds(), this.maximumClockDifference.getMilliSeconds());
        }
    }

    public Optional<TimeDuration> getTimeDifference() {
        return Optional.ofNullable(timeDifference);
    }

    @Override
    public String getDescriptionTitle() {
        return "Verify the device time difference";
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.VERIFY_TIME_DIFFERENCE_COMMAND;
    }

}