package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.rest.ValidationRuleInfo;

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

    public DetailedValidationInfo() {

    }

}
