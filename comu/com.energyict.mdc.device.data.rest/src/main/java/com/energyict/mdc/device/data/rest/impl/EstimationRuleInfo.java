/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.ArrayList;
import java.util.List;

public class EstimationRuleInfo {

    public long id;

    public long ruleSetId;

    public String name;

    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();

    public Boolean deleted;

    public IdWithNameInfo application;

}
