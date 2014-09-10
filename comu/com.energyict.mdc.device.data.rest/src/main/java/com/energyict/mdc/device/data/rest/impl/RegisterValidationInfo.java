package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import java.util.*;

public class RegisterValidationInfo {

    public Boolean validationStatus;
    public Boolean dataValidated;
    public Map<ValidationRuleInfo, Long> suspectReason;
    public Date lastChecked;

    public RegisterValidationInfo() {

    }

    public RegisterValidationInfo(Boolean validationStatus, List<DataValidationStatus> dataValidationStatuses, Date lastChecked) {
        this.validationStatus = validationStatus;
        if (validationStatus) {
            this.dataValidated = isDataCompletelyValidated(dataValidationStatuses);
            this.suspectReason = getSuspectReasonMap(dataValidationStatuses);
            this.lastChecked = lastChecked;
        }
    }

    private boolean isDataCompletelyValidated(List<DataValidationStatus> dataValidationStatuses) {
        for(DataValidationStatus status : dataValidationStatuses) {
            if(!status.completelyValidated()) {
                return false;
            }
        }
        return true;
    }

    private Map<ValidationRuleInfo, Long> getSuspectReasonMap(List<DataValidationStatus> dataValidationStatuses) {
        Map<ValidationRule, Long> suspectReasonMap = new HashMap<>();
        for(DataValidationStatus dataValidationStatus : dataValidationStatuses) {
            Collection<ValidationRule> validationRules = dataValidationStatus.getOffendedRules();
            fillSuspectReasonMap(validationRules, suspectReasonMap);
        }
        return ValidationRuleInfo.from(suspectReasonMap);
    }

    private void fillSuspectReasonMap(Collection<ValidationRule> validationRules, Map<ValidationRule, Long> suspectReasonMap) {
        for(ValidationRule validationRule : validationRules) {
            if(suspectReasonMap.containsKey(validationRule)) {
                Long number = suspectReasonMap.get(validationRule);
                suspectReasonMap.put(validationRule, ++number);
            } else {
                suspectReasonMap.put(validationRule, 1L);
            }
        }
    }
}
