package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Map;

/**
 * Created by tgr on 5/09/2014.
 */
public class DetailedValidationInfo {

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;
    public boolean dataValidated;
    public Map<ValidationRuleInfo, Integer> validationRules;

    public DetailedValidationInfo(DataValidationStatus value, ValidationEvaluator evaluator) {
        dataValidated = value.completelyValidated();
        validationResult = ValidationStatus.forResult(evaluator.getValidationResult(value.getReadingQualities()));
    }

    public DetailedValidationInfo() {

    }
}
