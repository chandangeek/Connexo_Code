package com.elster.insight.usagepoint.data.rest.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.elster.insight.common.rest.IntervalInfo;
import com.elster.insight.usagepoint.data.rest.BigDecimalAsStringAdapter;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by bvn on 8/1/14.
 */
public class RegisterDataInfo {
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("readingTime")
    public Instant readingTime;
//    @JsonProperty("reportedDateTime")
//    public Instant reportedDateTime;
    
    @JsonProperty("modificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag modificationFlag;
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;
//    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
//    public BigDecimal collectedValue;
//    public boolean isBulk;
    public BigDecimal deltaValue;
    public String readingType;
    
    @JsonProperty("validationStatus")
    public Boolean validationStatus;
    @JsonProperty("dataValidated")
    public Boolean dataValidated;
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;
    @JsonProperty("suspectReason")
    public Set<ValidationRuleInfo> suspectReason;
    @JsonProperty("estimatedByRule")
    public EstimationRuleInfo estimatedByRule;
    @JsonProperty("isConfirmed")
    public Boolean isConfirmed;

//    @JsonProperty("validationInfo")
//    public VeeReadingInfo validationInfo;

//    public BaseReading createNew() {
//        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.value);
//    }
//
////    public BaseReading createNewBulk() {
////        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.collectedValue);
////    }
//
//    public BaseReading createConfirm() {
//        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), null);
//    }
}
