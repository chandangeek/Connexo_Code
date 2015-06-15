package com.elster.jupiter.validation.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

public class PropertyInfoFactory {

    public <T> Object asInfoObject(T property) {
        if (property instanceof ListValue) {
            ListValue<HasIdAndName> value = (ListValue<HasIdAndName>) property;
            List<Object> infos = new ArrayList<>();
            for (HasIdAndName entry : value.getValues()) {
                infos.add(entry.getId());
            }
            return infos;
        }
        if (property instanceof RelativePeriod) {
            return new RelativePeriodInfo((RelativePeriod) property);
        }
        return property;
    }

    public <T> Object asInfoObjectForPredifinedValues(T property) {
        if (property instanceof ListValue) {
            ListValue<HasIdAndName> value = (ListValue<HasIdAndName>) property;
            if (value.hasSingleValue()) {
                HasIdAndName entry = value.getValue();
                return asInfo(entry.getId(), entry.getName());
            }
        }
        return property;
    }
    
    private <T> Object asInfo(Object id, String name) {
        LinkedHashMap<String, Object> info = new LinkedHashMap<>();
        info.put("id", id);
        info.put("name", name);
        return info;
    }
}
