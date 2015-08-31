package com.elster.insight.usagepoint.data.rest.impl;

import java.util.List;

import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;

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
