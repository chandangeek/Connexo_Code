package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.properties.HasIdAndName;

import java.util.List;
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
        return property;
    }

}