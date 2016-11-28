package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.IntervalInfo;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

public class OutputChannelDataInfo {

    public IntervalInfo interval;

    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal editedValue;

    public Instant readingTime;

    public Boolean dataValidated;

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    public ValidationAction action;

    public Set<ValidationRuleInfo> validationRules;

    public Boolean isConfirmed;

    public Boolean isEdited;

    public BaseReading createNew() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.value, Collections.emptyList());
    }

    public BaseReading createConfirm() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), null, Collections.emptyList());
    }
}
