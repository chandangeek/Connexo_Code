/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.util.List;

public class MonitorValidationInfo {


    public ValidationStatusInfo validationStatus;
    public Long total;
    public List<DetailedValidationRuleSetInfo> detailedRuleSets;
    public List<DetailedValidationLoadProfileInfo> detailedValidationLoadProfile;
    public List<DetailedValidationRegisterInfo> detailedValidationRegister;

    public MonitorValidationInfo() {
    }

}
