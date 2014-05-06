package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
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

    public MarkIntervalsAsBadTimeCommandImpl(final LoadProfileCommand loadProfileCommand, final CommandRoot commandRoot) {
        super(commandRoot);
        this.loadProfileCommand = loadProfileCommand;
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("minimumClockDifference").append(loadProfileCommand.getLoadProfilesTask().getMinClockDiffBeforeBadTime());
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            this.appendBadTimeLoadProfiles(builder);
        }
    }

    private void appendBadTimeLoadProfiles (DescriptionBuilder descriptionBuilder) {
        if (this.badTimeLoadProfiles.isEmpty()) {
            descriptionBuilder.addLabel("No intervals exceeded the maximum allowed time difference.");
        }
        else {
            PropertyDescriptionBuilder builder = descriptionBuilder.addListProperty("badTimeLoadProfiles");
            for (DeviceLoadProfile deviceLoadProfile : badTimeLoadProfiles) {
                builder = builder.append(deviceLoadProfile.getLoadProfileIdentifier()).next();
            }
            descriptionBuilder.addProperty("actualTimeDifference").append(this.loadProfileCommand.getTimeDifferenceCommand().getTimeDifference());
        }
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        if (this.hasLargerOrEqualDuration()) {
            for (CollectedData collectedData : loadProfileCommand.getCollectedData()) {
                if (collectedData instanceof DeviceLoadProfile) {
                    DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) collectedData;
                    for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
                        intervalData.addEiStatus(IntervalStateBits.BADTIME);
                    }
                    this.badTimeLoadProfiles.add(deviceLoadProfile);
                }
            }
        }
    }

    private boolean hasLargerOrEqualDuration() {
        return TimeDurations.hasLargerOrEqualDurationThen(
                loadProfileCommand.getTimeDifferenceCommand().getTimeDifference().abs(),
                loadProfileCommand.getLoadProfilesTask().getMinClockDiffBeforeBadTime(), true);
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.MARK_LOAD_PROFILES_AS_BAD_TIME;
    }

}