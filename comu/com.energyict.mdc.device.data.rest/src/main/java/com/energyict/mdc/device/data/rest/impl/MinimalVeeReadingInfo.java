package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class MinimalVeeReadingInfo {
    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("mainValidationInfo")
    public MinimalVeeReadingValueInfo mainValidationInfo = new MinimalVeeReadingValueInfo();

    @JsonProperty("bulkValidationInfo")
    public MinimalVeeReadingValueInfo bulkValidationInfo = new MinimalVeeReadingValueInfo();

    public MinimalVeeReadingInfo() {
    }
}

class MinimalVeeReadingValueInfo {
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    @JsonProperty("action")
    public ValidationAction action;

    @JsonProperty("estimatedByRule")
    public Boolean estimatedByRule;

    @JsonProperty("valueModificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag valueModificationFlag;

    @JsonProperty("isConfirmed")
    public Boolean isConfirmed;

    public MinimalVeeReadingValueInfo() {
    }
}
