package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertyInfoFactory {

    public <T> Object asInfoObject(T property) {
        if (property instanceof List) {
            List<HasIdAndName> value = (List<HasIdAndName>) property;
            return value.stream().map(HasIdAndName::getId).collect(Collectors.toList());
        }
        if (property instanceof HasIdAndName) {
            return ((HasIdAndName) property).getId();
        }
        return property;
    }

    public <T> Object asInfoObjectForPredifinedValues(T property) {
        if (property instanceof List) {
            List<HasIdAndName> value = (List<HasIdAndName>) property;
            if (value.size() == 1) {
                HasIdAndName entry = value.get(0);
                return asInfo(entry.getId(), entry.getName());
            }
        }
        if (property instanceof HasIdAndName) {
            HasIdAndName idWithName = (HasIdAndName)property;
            return asInfo(idWithName.getId(), idWithName.getName());
        }
        return property;
    }

    private Object asInfo(Object id, String name) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", id);
        info.put("name", name);
        return info;
    }

}