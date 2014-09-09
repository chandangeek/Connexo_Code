package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfileReading;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by bvn on 8/1/14.
 */
public class ChannelDataInfo {
    public IntervalInfo interval;
    public Date readingTime;
    public List<String> intervalFlags;
    public BigDecimal value;

    @JsonProperty("dataValidated")
    public Boolean dataValidated;
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;
    @JsonProperty("suspectReason")
    public List<ValidationRuleInfo> suspectReason;

    public static List<ChannelDataInfo> from(List<? extends LoadProfileReading> loadProfileReadings, Thesaurus thesaurus, ValidationEvaluator evaluator) {
        List<ChannelDataInfo> channelData = new ArrayList<>();
        for (LoadProfileReading loadProfileReading : loadProfileReadings) {
            ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
            channelIntervalInfo.interval=IntervalInfo.from(loadProfileReading.getInterval());
            channelIntervalInfo.readingTime=loadProfileReading.getReadingTime();
            channelIntervalInfo.intervalFlags=new ArrayList<>();
            for (ProfileStatus.Flag flag : loadProfileReading.getFlags()) {
                channelIntervalInfo.intervalFlags.add(thesaurus.getString(flag.name(), flag.name()));
            }
            for (Map.Entry<Channel, BigDecimal> entry : loadProfileReading.getChannelValues().entrySet()) {
                channelIntervalInfo.value=entry.getValue(); // There can be only one channel (or no channel at all if the channel has no dta for this interval)
            }
            Set<Map.Entry<Channel, DataValidationStatus>> states = loadProfileReading.getChannelValidationStates().entrySet();
            for (Map.Entry<Channel, DataValidationStatus> entry : states) {
                    ValidationInfo validationInfo = new ValidationInfo(entry.getValue(), evaluator);
                    channelIntervalInfo.validationResult = validationInfo.validationResult;
                    channelIntervalInfo.suspectReason = validationInfo.validationRules;
                    channelIntervalInfo.dataValidated = validationInfo.dataValidated;
            }
            channelData.add(channelIntervalInfo);
        }
        return channelData;
    }
}

