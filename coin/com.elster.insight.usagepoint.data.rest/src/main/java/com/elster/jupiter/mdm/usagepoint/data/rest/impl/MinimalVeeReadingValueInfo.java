package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-15 (16:01)
 */
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

    MinimalVeeReadingValueInfo() {
    }
}