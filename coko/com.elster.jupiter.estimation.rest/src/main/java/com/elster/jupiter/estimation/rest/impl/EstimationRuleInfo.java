/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EstimationRuleInfo {

    public long id;
    public boolean active;
    public boolean deleted;
    public String implementation; //estimator classname
    public String displayName; // readable name
    public String name;
    public List<PropertyInfo> properties = new ArrayList<>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<>();
    public EstimationRuleSetInfo ruleSet;
    public long version;
    public EstimationRuleSetInfo parent = new EstimationRuleSetInfo();

    public EstimationRuleInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((EstimationRuleInfo) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
