package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.Set;

public class NumericalReadingInfo extends ReadingInfo {
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal deltaValue;
    @JsonProperty("rawValue")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal rawValue;
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unitOfMeasure;
    @JsonProperty("validationStatus")
    public Boolean validationStatus;
    @JsonProperty("dataValidated")
    public Boolean dataValidated;
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;
    @JsonProperty("suspectReason")
    public Set<ValidationRuleInfo> suspectReason;
    @JsonProperty("estimationRules")
    public Set<EstimationRuleInfo> estimationRules;

    public NumericalReadingInfo() {}

    @Override
    protected BaseReading createNew(Register register) {
        return ReadingImpl.of(register.getReadingType().getMRID(), this.value, this.timeStamp);
    }

}