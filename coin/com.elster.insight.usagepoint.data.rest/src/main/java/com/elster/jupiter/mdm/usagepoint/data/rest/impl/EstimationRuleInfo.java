/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class EstimationRuleInfo {

    public long id;

    @JsonProperty("displayName")
    public String estimatorName;

    public long ruleSetId;

    public String name;

    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();

    public Boolean deleted;

    public IdWithNameInfo application;
}
