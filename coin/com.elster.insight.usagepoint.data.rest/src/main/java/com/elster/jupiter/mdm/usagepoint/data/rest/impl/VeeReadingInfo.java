package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by tgr on 5/09/2014.
 */
public class VeeReadingInfo {
    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("mainValidationInfo")
    public VeeReadingValueInfo mainValidationInfo = new VeeReadingValueInfo();

    @JsonProperty("bulkValidationInfo")
    public VeeReadingValueInfo bulkValidationInfo = new VeeReadingValueInfo();
    @JsonProperty("readingQualities")
    public List<IdWithNameInfo> readingQualities;

    public VeeReadingInfo() {
    }
}

class VeeReadingValueInfo {
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    @JsonProperty("validationRules")
    public Set<ValidationRuleInfo> validationRules = Collections.emptySet();

    @JsonProperty("estimatedByRule")
    public EstimationRuleInfo estimatedByRule;

    @JsonProperty("valueModificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag valueModificationFlag;

    @JsonProperty("isConfirmed")
    public Boolean isConfirmed;

    public VeeReadingValueInfo() {
    }
}
