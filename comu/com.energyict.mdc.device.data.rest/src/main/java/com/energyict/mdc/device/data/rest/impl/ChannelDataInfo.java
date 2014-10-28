package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

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
    @JsonProperty("editedTime")
    public Instant editedTime;
    @JsonProperty("intervalFlags")
    public List<String> intervalFlags;
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

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
            channelIntervalInfo.interval=IntervalInfo.from(loadProfileReading.getInterval());
            channelIntervalInfo.readingTime=loadProfileReading.getReadingTime();
            channelIntervalInfo.intervalFlags=new ArrayList<>();
            channelIntervalInfo.validationStatus = isValidationActive;
            for (ProfileStatus.Flag flag : loadProfileReading.getFlags()) {
                channelIntervalInfo.intervalFlags.add(thesaurus.getString(flag.name(), flag.name()));
            }
            Channel valueOwnerChannel = null;
            for (Map.Entry<Channel, IntervalReadingRecord> entry : loadProfileReading.getChannelValues().entrySet()) {
                valueOwnerChannel = entry.getKey();
                channelIntervalInfo.value=entry.getValue().getValue(); // There can be only one channel (or no channel at all if the channel has no dta for this interval)
                channelIntervalInfo.editedTime = entry.getValue().edited() ? entry.getValue().getReportedDateTime() : null;
            }
            if (channelIntervalInfo.value != null && valueOwnerChannel != null){
                channelIntervalInfo.value= channelIntervalInfo.value.setScale(valueOwnerChannel.getChannelSpec().getNbrOfFractionDigits(), BigDecimal.ROUND_UP);
            }
            Set<Map.Entry<Channel, DataValidationStatus>> states = loadProfileReading.getChannelValidationStates().entrySet();    //  only one channel
            for (Map.Entry<Channel, DataValidationStatus> entry : states) {
                    ValidationInfo validationInfo = new ValidationInfo(entry.getValue(), deviceValidation);
                    channelIntervalInfo.validationResult = validationInfo.validationResult;
                    channelIntervalInfo.suspectReason = validationInfo.validationRules;
                    channelIntervalInfo.dataValidated = validationInfo.dataValidated;
            }
            channelData.add(channelIntervalInfo);
        }
        return channelData;
    }

    public BaseReading createNew() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.value);
    }

}
