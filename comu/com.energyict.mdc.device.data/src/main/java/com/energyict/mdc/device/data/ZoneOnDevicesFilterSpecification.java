package com.energyict.mdc.device.data;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.HashMap;
import java.util.Map;

public class ZoneOnDevicesFilterSpecification {
    public Map<String, SearchablePropertyValue.ValueBean> properties = new HashMap<>();

    public SearchablePropertyValue.ValueBean getPropertyValue(SearchableProperty property) {
        return this.properties.get(property.getName());
    }
}
