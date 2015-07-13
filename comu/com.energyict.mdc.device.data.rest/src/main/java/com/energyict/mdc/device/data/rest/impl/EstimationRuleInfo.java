package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.ArrayList;
import java.util.List;

public class EstimationRuleInfo {

    public long id;

    public long ruleSetId;

    public String name;

    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();

    public Boolean deleted;

}
