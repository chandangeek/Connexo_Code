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
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.google.common.collect.Range;

/**
 * Groups functionality to create info objects for different sorts of data of a device
 */
public class UsagePointDataInfoFactory {

    private final Thesaurus thesaurus;
    private final Clock clock;
    private final ValidationInfoFactory validationInfoFactory;
    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;

    @Inject
    public UsagePointDataInfoFactory(Thesaurus thesaurus, Clock clock, ValidationInfoFactory validationInfoFactory, ValidationRuleInfoFactory validationRuleInfoFactory, EstimationRuleInfoFactory estimationRuleInfoFactory) {
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.validationInfoFactory = validationInfoFactory;
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
    }

    public ChannelDataInfo createChannelDataInfo(BaseReadingRecord brr, boolean validationEnabled, Channel channel, UsagePointValidation upv) {
        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();

        Instant start = brr.getTimeStamp().minus(Duration.ofMinutes(brr.getReadingType().getMeasuringPeriod().getMinutes()));
        Instant end = brr.getTimeStamp();
        
        
        Range<Instant> range = Ranges.openClosed(start, end);
        channelIntervalInfo.interval = IntervalInfo.from(range);
        channelIntervalInfo.readingTime = brr.getReportedDateTime();//.getReadingTime();
        channelIntervalInfo.intervalFlags = new ArrayList<>();
        IntervalReadingRecord irr = (IntervalReadingRecord) brr;

        channelIntervalInfo.intervalFlags.addAll(getFlagsFromProfileStatus(irr.getProfileStatus()).stream().map(flag -> thesaurus.getString(flag.name(), flag.name())).collect(Collectors.toList()));

        channelIntervalInfo.value = brr.getValue();
        channelIntervalInfo.validationStatus = upv.isValidationActive();

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
    
    public VeeReadingInfo createVeeReadingInfoWithModificationFlags(Channel channel, DataValidationStatus dataValidationStatus, UsagePointValidation upv, BaseReadingRecord realReading) {
        return validationInfoFactory.createVeeReadingInfoWithModificationFlags(channel, dataValidationStatus, upv, (IntervalReadingRecord)realReading);
        
    }

    public RegisterDataInfo createRegisterDataInfo(BaseReadingRecord brr, boolean validationEnabled, Channel channel, UsagePointValidation upv) {
        RegisterDataInfo registerDataInfo = new RegisterDataInfo();
        registerDataInfo.id = brr.getTimeStamp();
        registerDataInfo.readingTime = brr.getTimeStamp();
        registerDataInfo.reportedDateTime = brr.getReportedDateTime();
        registerDataInfo.modificationFlag = ReadingModificationFlag.getModificationFlag((ReadingRecord) brr);
        
        registerDataInfo.value = brr.getValue();
        
        
        registerDataInfo.validationStatus = upv.isValidationActive();
        
        List<BaseReadingRecord> fakeList = Arrays.asList(brr);
        
        Range<Instant> range = Ranges.closedOpen(brr.getTimeStamp(), brr.getTimeStamp().plusSeconds(1));
        
        Optional<DataValidationStatus> validationStatus = upv.getValidationStatus(channel, fakeList, range).stream().findFirst();
        validationStatus.ifPresent(status -> {
            registerDataInfo.dataValidated = status.completelyValidated();
            registerDataInfo.validationResult = ValidationStatus.forResult(ValidationResult.getValidationResult(status.getReadingQualities()));
            registerDataInfo.suspectReason = validationRuleInfoFactory.createInfosForDataValidationStatus(status);
            registerDataInfo.estimatedByRule = estimationRuleInfoFactory.createEstimationRuleInfo(status.getReadingQualities());
            registerDataInfo.isConfirmed = validationInfoFactory.isConfirmedData(brr, status.getReadingQualities());
        });
        
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
