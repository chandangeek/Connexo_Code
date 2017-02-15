/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UsagePointFilter {

    private Map<String, SearchablePropertyValue.ValueBean> properties;

    public UsagePointFilter() {
        properties = Collections.emptyMap();
    }

    public UsagePointFilter(Map<String, SearchablePropertyValue.ValueBean> properties) {
        this.properties = new HashMap<>(properties);
    }

    public SearchablePropertyValue.ValueBean getPropertyValue(SearchableProperty property) {
        return this.properties.get(property.getName());
    }

    public Map<String, SearchablePropertyValue.ValueBean> getProperties() {
        return ImmutableMap.copyOf(properties);
    }

    public void setProperties(Map<String, SearchablePropertyValue.ValueBean> properties) {
        this.properties = new HashMap<>(properties);
    }
}
