package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EstimationRuleInfo {

    public long id;

    public String displayName;

    public long ruleSetId;

    public String name;

    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();

    public Boolean deleted;

    public IdWithNameInfo application;

    public Instant when;
}
