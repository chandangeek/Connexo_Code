package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Created by bvn on 8/1/14.
 */
public class ChannelDataInfo {
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("readingTime")
    public Instant readingTime;
    @JsonProperty("modificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag modificationFlag;
    @JsonProperty("reportedDateTime")
    public Instant reportedDateTime;
    @JsonProperty("intervalFlags")
    public List<String> intervalFlags;
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal collectedValue;
    public boolean isBulk;

    @JsonProperty("validationStatus")
    public Boolean validationStatus;

    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    @JsonProperty("suspectReason")
    public Set<ValidationRuleInfo> suspectReason;

    public static List<ChannelDataInfo> from(List<? extends LoadProfileReading> loadProfileReadings, boolean isValidationActive, Thesaurus thesaurus, DeviceValidation deviceValidation) {
        List<ChannelDataInfo> channelData = new ArrayList<>();
        for (LoadProfileReading loadProfileReading : loadProfileReadings) {
            ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
            channelIntervalInfo.interval=IntervalInfo.from(loadProfileReading.getRange());
            channelIntervalInfo.readingTime=loadProfileReading.getReadingTime();
            channelIntervalInfo.intervalFlags=new ArrayList<>();
            channelIntervalInfo.validationStatus = isValidationActive;
            for (ProfileStatus.Flag flag : loadProfileReading.getFlags()) {
                channelIntervalInfo.intervalFlags.add(thesaurus.getString(flag.name(), flag.name()));
            }
            for (Map.Entry<Channel, IntervalReadingRecord> entry : loadProfileReading.getChannelValues().entrySet()) {
                channelIntervalInfo.isBulk = entry.getKey().getReadingType().isCumulative();
                channelIntervalInfo.value = getRoundedBigDecimal(entry.getValue().getValue(), entry.getKey()); // There can be only one channel (or no channel at all if the channel has no dta for this interval)
                if (channelIntervalInfo.isBulk) {
                    Quantity quantity = entry.getValue().getQuantity(entry.getKey().getReadingType());
                    channelIntervalInfo.collectedValue = quantity != null ?  quantity.getValue() : null;
                }
                channelIntervalInfo.modificationFlag = ReadingModificationFlag.getFlag(entry.getValue());
                channelIntervalInfo.reportedDateTime = entry.getValue().getReportedDateTime();
                channelIntervalInfo.collectedValue = getRoundedBigDecimal(channelIntervalInfo.collectedValue, entry.getKey());
            }
            if (loadProfileReading.getChannelValues().isEmpty() && loadProfileReading.getReadingTime() != null) {
                channelIntervalInfo.modificationFlag = ReadingModificationFlag.REMOVED;
                channelIntervalInfo.reportedDateTime = loadProfileReading.getReadingTime();
            }

            Set<Map.Entry<Channel, DataValidationStatus>> states = loadProfileReading.getChannelValidationStates().entrySet();    //  only one channel
            for (Map.Entry<Channel, DataValidationStatus> entry : states) {
                    ValidationInfo validationInfo = new ValidationInfo(entry.getValue(), deviceValidation);
                    channelIntervalInfo.validationResult = validationInfo.validationResult;
                    channelIntervalInfo.suspectReason = validationInfo.validationRules;
                    channelIntervalInfo.dataValidated = validationInfo.dataValidated;
            }
            if (loadProfileReading.getChannelValues().isEmpty() && loadProfileReading.getChannelValidationStates().isEmpty()) {
                // we have a reading with no data and no validation result => it's a placeholder (missing value) which hasn't validated ( = detected ) yet
                channelIntervalInfo.validationResult = ValidationStatus.NOT_VALIDATED;
                channelIntervalInfo.suspectReason = Collections.EMPTY_SET;
                channelIntervalInfo.dataValidated = false;
            }
            channelData.add(channelIntervalInfo);
        }
        return channelData;
    }

    public BaseReading createNew() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.value);
    }

    private static BigDecimal getRoundedBigDecimal(BigDecimal value, Channel channel) {
        return value != null ? value.setScale(channel.getChannelSpec().getNbrOfFractionDigits(), BigDecimal.ROUND_UP) : value;
    }
}
