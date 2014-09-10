package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.Set;

/**
 * Created by tgr on 5/09/2014.
 */
public class ValidationInfo {

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;
    public boolean dataValidated;
    public Set<ValidationRuleInfo> validationRules;

    public ValidationInfo(DataValidationStatus value, ValidationEvaluator evaluator) {
        dataValidated = value.completelyValidated();
        validationRules = ValidationRuleInfo.from(value);
        validationResult = ValidationStatus.forResult(evaluator.getValidationResult(value.getReadingQualities()));
    }

}
