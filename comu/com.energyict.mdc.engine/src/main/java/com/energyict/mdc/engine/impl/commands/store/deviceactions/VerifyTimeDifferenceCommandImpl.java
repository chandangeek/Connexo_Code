/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.BasicCheckCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.VerifyTimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;

import java.util.Optional;

import static java.lang.Math.toIntExact;

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

    public VerifyTimeDifferenceCommandImpl(BasicCheckCommand basicCheckCommand, final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
        this.basicCheckCommand = basicCheckCommand;
        maximumClockDifference = basicCheckCommand.getMaximumClockDifference().orElseGet(() -> TimeDuration.millis(0));
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        TimeDuration maxDiffInSeconds = new TimeDuration(this.basicCheckCommand.getMaximumClockDifference().orElse(new TimeDuration(0)).getSeconds(), TimeDuration.TimeUnit.SECONDS);
        builder.addProperty("maximumDifference").append(maxDiffInSeconds);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.timeDifference = this.basicCheckCommand.getTimeDifference().orElse(TimeDuration.TimeUnit.MILLISECONDS.during(0));

        if (Math.abs(this.timeDifference.getMilliSeconds()) > this.maximumClockDifference.getMilliSeconds()) {
            addIssue(getIssueService().newProblem(
                    getCommandType(),
                    MessageSeeds.MAXIMUM_TIME_DIFFERENCE_EXCEEDED,
                    new TimeDuration(toIntExact(this.maximumClockDifference.getMilliSeconds()), TimeDuration.TimeUnit.MILLISECONDS),
                    new TimeDuration(toIntExact(Math.abs(this.timeDifference.getMilliSeconds())), TimeDuration.TimeUnit.MILLISECONDS)),
                    CompletionCode.TimeError);
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
    public ComCommandType getCommandType() {
        return ComCommandTypes.VERIFY_TIME_DIFFERENCE_COMMAND;
    }

}