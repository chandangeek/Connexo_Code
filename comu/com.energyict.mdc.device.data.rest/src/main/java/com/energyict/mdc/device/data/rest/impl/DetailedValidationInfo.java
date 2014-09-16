package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tgr on 5/09/2014.
 */
public class DetailedValidationInfo {

    public Boolean validationActive;
    public Boolean dataValidated;
    public Set<Map.Entry<ValidationRuleInfo, Long>> suspectReason;
    public Long lastChecked;

    public DetailedValidationInfo(Boolean active, List<DataValidationStatus> dataValidationStatuses, Date lastChecked) {
        validationActive = active;
        if (validationActive) {
            this.dataValidated = isDataCompletelyValidated(dataValidationStatuses);
            this.suspectReason = getSuspectReasonMap(dataValidationStatuses).entrySet();
            this.lastChecked = lastChecked == null ? null : lastChecked.getTime();
        }
    }

    public DetailedValidationInfo() {

    }
    private boolean isDataCompletelyValidated(List<DataValidationStatus> dataValidationStatuses) {
        return dataValidationStatuses.stream().allMatch(DataValidationStatus::completelyValidated);
    }

    private Map<ValidationRuleInfo, Long> getSuspectReasonMap(List<DataValidationStatus> dataValidationStatuses) {
        Map<ValidationRule, Long> suspectReasonMap = new HashMap<>();
        dataValidationStatuses.stream().forEach(s -> fillSuspectReasonMap(s.getOffendedRules(), suspectReasonMap));
        return ValidationRuleInfo.from(suspectReasonMap);
    }

    private void fillSuspectReasonMap(Collection<ValidationRule> validationRules, Map<ValidationRule, Long> suspectReasonMap) {
        validationRules.forEach(r -> {
            suspectReasonMap.putIfAbsent(r, 0L);
            suspectReasonMap.compute(r, (k, v) -> v + 1);
        });
    }
}
