package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import java.util.List;

/**
 * Created by adrianlupan on 4/22/15.
 */
public class DetailedValidationRuleSetVersionInfo extends ValidationRuleSetVersionInfo {

    public long total;
    public List<DetailedValidationRuleInfo> detailedRules;


    public DetailedValidationRuleSetVersionInfo() {

    }

}
