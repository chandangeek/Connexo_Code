package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.elster.insight.common.rest.IntervalInfo;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Ranges;
import com.google.common.collect.Range;

/**
 * Groups functionality to create info objects for different sorts of data of a device
 */
public class UsagePointDataInfoFactory {

    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    public UsagePointDataInfoFactory(Thesaurus thesaurus, Clock clock) {
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    public ChannelDataInfo createChannelDataInfo(BaseReadingRecord brr) {
        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();

        Range<Instant> range = Ranges.openClosed(brr.getTimeStamp(), brr.getTimeStamp().plus(Duration.ofMinutes(brr.getReadingType().getMeasuringPeriod().getMinutes())));
        channelIntervalInfo.interval = IntervalInfo.from(range);
        channelIntervalInfo.readingTime = brr.getReportedDateTime();//.getReadingTime();
        channelIntervalInfo.intervalFlags = new ArrayList<>();
        IntervalReadingRecord irr = (IntervalReadingRecord) brr;

        channelIntervalInfo.intervalFlags.addAll(getFlagsFromProfileStatus(irr.getProfileStatus()).stream().map(flag -> thesaurus.getString(flag.name(), flag.name())).collect(Collectors.toList()));

        channelIntervalInfo.value = brr.getValue();

        return channelIntervalInfo;
    }

    public RegisterDataInfo createRegisterDataInfo(BaseReadingRecord brr) {
        RegisterDataInfo registerDataInfo = new RegisterDataInfo();
        registerDataInfo.readingTime = brr.getReportedDateTime();
        registerDataInfo.value = brr.getValue();
        return registerDataInfo;
    }

    private List<ProfileStatus.Flag> getFlagsFromProfileStatus(ProfileStatus profileStatus) {
        List<ProfileStatus.Flag> flags = new ArrayList<>();
        if (profileStatus == null)
            return flags;
        for (ProfileStatus.Flag flag : ProfileStatus.Flag.values()) {
            if (profileStatus.get(flag)) {
                flags.add(flag);
            }
        }
        return flags;
    }

}