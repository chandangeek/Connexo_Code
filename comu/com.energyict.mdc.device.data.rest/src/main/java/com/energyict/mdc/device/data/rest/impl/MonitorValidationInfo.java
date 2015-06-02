package com.energyict.mdc.device.data.rest.impl;

import java.util.*;

public class MonitorValidationInfo {


    public ValidationStatusInfo validationStatus;
    public Long total;
    public List<DetailedValidationRuleSetInfo> detailedRuleSets;
    public List<DetailedValidationLoadProfileInfo> detailedValidationLoadProfile;
    public List<DetailedValidationRegisterInfo> detailedValidationRegister;

    public MonitorValidationInfo() {
    }

}
