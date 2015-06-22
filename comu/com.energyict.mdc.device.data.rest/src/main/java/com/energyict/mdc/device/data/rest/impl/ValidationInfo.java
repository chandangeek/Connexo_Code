package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Set;

/**
 * Created by tgr on 5/09/2014.
 */
public class ValidationInfo {
    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("mainValidationInfo")
    public ValidationReadingInfo mainValidationInfo = new ValidationReadingInfo();

    @JsonProperty("bulkValidationInfo")
    public ValidationReadingInfo bulkValidationInfo = new ValidationReadingInfo();

    public ValidationInfo() {

    }
}
class ValidationReadingInfo {
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    @JsonProperty("validationRules")
    public Set<ValidationRuleInfo> validationRules;

    public ValidationReadingInfo() {

    }
}
