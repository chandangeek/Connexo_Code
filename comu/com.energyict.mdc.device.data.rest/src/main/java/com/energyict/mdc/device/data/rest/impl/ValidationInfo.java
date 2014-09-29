package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.device.data.DeviceValidation;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Set;

/**
 * Created by tgr on 5/09/2014.
 */
public class ValidationInfo {

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;
    public boolean dataValidated;
    public Set<ValidationRuleInfo> validationRules;

    public ValidationInfo(DataValidationStatus value, DeviceValidation deviceValidation) {
            dataValidated = value.completelyValidated();
            validationRules = ValidationRuleInfo.from(value);
            validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(value.getReadingQualities()));
    }

    public ValidationInfo() {

    }
}
