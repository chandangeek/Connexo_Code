package com.energyict.mdc.device.data;

import com.elster.jupiter.search.SearchableProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComScheduleOnDevicesFilterSpecification {

    public Map<String, String> singleProperties = new HashMap<>();
    public Map<String, List<String>> listProperties = new HashMap<>();

    public Object getPropertyValue(SearchableProperty property) {
        if (singleProperties.containsKey(property.getName())) {
            return singleProperties.get(property.getName());
        } else if (listProperties.containsKey(property.getName())) {
            return listProperties.get(property.getName());
        } else {
            return null;
        }
    }
}
