package com.elster.insight.usagepoint.data.rest.impl;

import java.util.List;

public class MonitorValidationInfo {


    public ValidationStatusInfo validationStatus;
    public Long total;
    public List<DetailedValidationRuleSetInfo> detailedRuleSets;
    public List<DetailedValidationChannelInfo> detailedValidationChannel;
    public List<DetailedValidationRegisterInfo> detailedValidationRegister;

    public MonitorValidationInfo() {
    }

}
