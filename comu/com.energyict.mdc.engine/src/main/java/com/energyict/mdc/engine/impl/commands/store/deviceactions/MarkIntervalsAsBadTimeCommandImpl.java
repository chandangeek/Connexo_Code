/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.engine.impl.tools.TimeDurations;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.IntervalData;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple command that will get the timeDifference between the device and the HeadEndSystem and based on
 * the {@link com.energyict.mdc.tasks.LoadProfilesTask#getMinClockDiffBeforeBadTime()}, we mark <b>ALL</b>
 * intervals of <b>ALL</b> the LoadProfiles
 */
public class MarkIntervalsAsBadTimeCommandImpl extends SimpleComCommand implements MarkIntervalsAsBadTimeCommand {

    private LoadProfileCommand loadProfileCommand;
    private List<DeviceLoadProfile> badTimeLoadProfiles = new ArrayList<>();

    public MarkIntervalsAsBadTimeCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final LoadProfileCommand loadProfileCommand) {
        super(groupedDeviceCommand);
        this.loadProfileCommand = loadProfileCommand;
    }

    @Override
    public String getDescriptionTitle() {
        return "Mark load profile intervals as bad time";
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if (this.hasLargerOrEqualDuration()) {
            TimeDuration diffInSeconds = loadProfileCommand.getTimeDifferenceCommand().getTimeDifference()
                    .map(TimeDuration::getSeconds)
                    .map(TimeDuration::seconds)
                    .orElse(TimeDuration.seconds(0));
            TimeDuration maxDiffInSeconds = loadProfileCommand.getLoadProfilesTaskOptions().getMinClockDiffBeforeBadTime()
                    .map(TimeDuration::getSeconds)
                    .map(TimeDuration::seconds)
                    .orElse(TimeDuration.seconds(0));
            addIssue(getIssueService().newWarning(getCommandType(), MessageSeeds.INTERVALS_MARKED_AS_BAD_TIME, diffInSeconds, maxDiffInSeconds), CompletionCode.ConfigurationWarning);

            for (CollectedData collectedData : loadProfileCommand.getCollectedData()) {
                if (collectedData instanceof DeviceLoadProfile) {
                    DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) collectedData;
                    for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
                        intervalData.addReadingQualityType(ProtocolReadingQualities.BADTIME.getReadingQualityType());
                    }
                    this.badTimeLoadProfiles.add(deviceLoadProfile);
                }
            }
        }
    }

    private boolean hasLargerOrEqualDuration() {
        return TimeDurations.hasLargerOrEqualDurationThen(
                loadProfileCommand.getTimeDifferenceCommand().getTimeDifference().orElse(TimeDuration.seconds(0)).abs(),
                loadProfileCommand.getLoadProfilesTaskOptions().getMinClockDiffBeforeBadTime().orElse(TimeDuration.seconds(0)), true);
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            builder.addProperty("minimumClockDifference").append(loadProfileCommand.getLoadProfilesTaskOptions().getMinClockDiffBeforeBadTime().orElse(TimeDuration.seconds(0)));
        }
        this.appendBadTimeLoadProfiles(builder);
    }

    private void appendBadTimeLoadProfiles(DescriptionBuilder descriptionBuilder) {
        PropertyDescriptionBuilder propertyDescriptionBuilder = descriptionBuilder.addListProperty("badTimeLoadProfiles");
        if (this.badTimeLoadProfiles.isEmpty()) {
            propertyDescriptionBuilder.append("None");
        } else {
            for (DeviceLoadProfile deviceLoadProfile : badTimeLoadProfiles) {
                propertyDescriptionBuilder.append(deviceLoadProfile.getLoadProfileIdentifier()).next();
            }
        }
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.MARK_LOAD_PROFILES_AS_BAD_TIME;
    }

}
