package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.elster.insight.common.rest.IntervalInfo;
import com.elster.insight.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;

/**
 * Groups functionality to create info objects for different sorts of data of a device
 */
public class UsagePointDataInfoFactory {

    private final Thesaurus thesaurus;
    private final Clock clock;
    private final ValidationInfoFactory validationInfoFactory;

    @Inject
    public UsagePointDataInfoFactory(Thesaurus thesaurus, Clock clock, ValidationInfoFactory validationInfoFactory) {
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.validationInfoFactory = validationInfoFactory;
    }

    public ChannelDataInfo createChannelDataInfo(BaseReadingRecord brr, boolean validationEnabled, Channel channel, UsagePointValidation upv) {
        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();

        Instant start = brr.getTimeStamp();
        Instant end = brr.getTimeStamp().plus(Duration.ofMinutes(brr.getReadingType().getMeasuringPeriod().getMinutes()));
        
        Range<Instant> range = Ranges.closedOpen(start, end);
        channelIntervalInfo.interval = IntervalInfo.from(range);
        channelIntervalInfo.readingTime = brr.getReportedDateTime();//.getReadingTime();
        channelIntervalInfo.intervalFlags = new ArrayList<>();
        IntervalReadingRecord irr = (IntervalReadingRecord) brr;

        channelIntervalInfo.intervalFlags.addAll(getFlagsFromProfileStatus(irr.getProfileStatus()).stream().map(flag -> thesaurus.getString(flag.name(), flag.name())).collect(Collectors.toList()));

        channelIntervalInfo.value = brr.getValue();
        channelIntervalInfo.validationStatus = upv.isValidationActive();
//        System.out.println(channelIntervalInfo.value);

        List<BaseReadingRecord> fakeList = Arrays.asList(brr);
      
        Optional<DataValidationStatus> validationStatus = upv.getValidationStatus(channel, fakeList, range).stream().findFirst();
        validationStatus.ifPresent(status -> {
            channelIntervalInfo.mainValidationInfo = validationInfoFactory.createMainVeeReadingInfo(status, upv, irr);
            channelIntervalInfo.bulkValidationInfo = validationInfoFactory.createBulkVeeReadingInfo(channel, status, upv, irr);
            channelIntervalInfo.dataValidated = true;
        });
        if (brr.getValue()==null && !validationStatus.isPresent()) {
            // we have a reading with no data and no validation result => it's a placeholder (missing value) which hasn't validated ( = detected ) yet
            channelIntervalInfo.mainValidationInfo = new MinimalVeeReadingValueInfo();
            channelIntervalInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            channelIntervalInfo.dataValidated = false;
        }
        return channelIntervalInfo;
    }
    
    
    
//    public ChannelDataInfo createChannelDataInfo(Channel channel, LoadProfileReading loadProfileReading, boolean isValidationActive, DeviceValidation deviceValidation) {
//        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
//        channelIntervalInfo.interval = IntervalInfo.from(loadProfileReading.getRange());
//        channelIntervalInfo.readingTime = loadProfileReading.getReadingTime();
//        channelIntervalInfo.intervalFlags = new ArrayList<>();
//        channelIntervalInfo.validationStatus = isValidationActive;
//        channelIntervalInfo.intervalFlags.addAll(loadProfileReading.getFlags().stream().map(flag -> thesaurus.getString(flag.name(), flag.name())).collect(Collectors.toList()));
//        Optional<IntervalReadingRecord> channelReading = loadProfileReading.getChannelValues().entrySet().stream().map(Map.Entry::getValue).findFirst();// There can be only one channel (or no channel at all if the channel has no dta for this interval)
//
//        channelReading.ifPresent(reading -> {
//            channelIntervalInfo.value = getRoundedBigDecimal(reading.getValue(), channel);
//            channel.getReadingType().getCalculatedReadingType().ifPresent(calculatedReadingType -> {
//                channelIntervalInfo.isBulk = true;
//                channelIntervalInfo.collectedValue = channelIntervalInfo.value;
//                Quantity quantity = reading.getQuantity(calculatedReadingType);
//                channelIntervalInfo.value = getRoundedBigDecimal(quantity != null ? quantity.getValue() : null, channel);
//            });
//            channelIntervalInfo.reportedDateTime = reading.getReportedDateTime();
//        });
//        if (!channelReading.isPresent() && loadProfileReading.getReadingTime() != null) {
//            channelIntervalInfo.reportedDateTime = loadProfileReading.getReadingTime();
//        }
//
//        Optional<DataValidationStatus> dataValidationStatus = loadProfileReading.getChannelValidationStates().entrySet().stream().map(Map.Entry::getValue).findFirst();
//        dataValidationStatus.ifPresent(status -> {
//            channelIntervalInfo.mainValidationInfo = validationInfoFactory.createMainVeeReadingInfo(status, deviceValidation, channelReading.orElse(null));
//            channelIntervalInfo.bulkValidationInfo = validationInfoFactory.createBulkVeeReadingInfo(channel, status, deviceValidation, channelReading.orElse(null));
//        });
//        if (!channelReading.isPresent() && !dataValidationStatus.isPresent()) {
//            // we have a reading with no data and no validation result => it's a placeholder (missing value) which hasn't validated ( = detected ) yet
//            channelIntervalInfo.mainValidationInfo = new MinimalVeeReadingValueInfo();
//            channelIntervalInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
//            if(channelIntervalInfo.isBulk) {
//                channelIntervalInfo.bulkValidationInfo = new MinimalVeeReadingValueInfo();
//                channelIntervalInfo.bulkValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
//            }
//            channelIntervalInfo.dataValidated = false;
//        }
//        return channelIntervalInfo;
//    }

    public RegisterDataInfo createRegisterDataInfo(BaseReadingRecord brr) {
        RegisterDataInfo registerDataInfo = new RegisterDataInfo();
        registerDataInfo.readingTime = brr.getTimeStamp();
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
