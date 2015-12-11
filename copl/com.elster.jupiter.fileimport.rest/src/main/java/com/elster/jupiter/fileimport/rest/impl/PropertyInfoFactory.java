package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;

import java.util.LinkedHashMap;
import java.util.Map;

public class PropertyInfoFactory {


    public <T> Object asInfoObject(T property) {
        if (property instanceof HasIdAndName) {
            return ((HasIdAndName)property).getId();
        }
        return property;
    }

    public <T> Object asInfoObjectForPredifinedValues(T property) {
        if (property instanceof HasIdAndName) {
            HasIdAndName idWithName = (HasIdAndName)property;
            return asInfo(idWithName.getId(), idWithName.getName());
        }
        return property;
    }

    private <T> Object asInfo(Object id, String name) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", id);
        info.put("name", name);
        return info;
    }

}