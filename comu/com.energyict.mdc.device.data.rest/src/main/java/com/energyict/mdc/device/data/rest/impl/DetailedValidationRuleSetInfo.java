package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;

import java.util.List;

/**
 * Created by adrianlupan on 4/22/15.
 */
public class DetailedValidationRuleSetInfo extends ValidationRuleSetInfo{

    public long id;
    public long total;
    public String name;
    public List<DetailedValidationRuleSetVersionInfo> detailedRuleSetVersions;


    public DetailedValidationRuleSetInfo() {

    }
}
