package com.elster.jupiter.issue.rest.response;

import java.util.ArrayList;
import java.util.List;

import com.elster.jupiter.properties.IdWithNameValue;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.rest.util.properties.IdWithNameInfo;

public class PropertyInfoFactory {

    public <T> Object asInfoObject(T property) {
        if (property instanceof ListValue) {
            ListValue<IdWithNameValue> value = (ListValue<IdWithNameValue>) property;
            List<Object> infos = new ArrayList<>();
            for (IdWithNameValue entry : value.getValues()) {
                infos.add(entry.getId());
            }
            return infos;
        }
        if (property instanceof IdWithNameValue) {
            return asInfo((IdWithNameValue)property);
        }
        return property;
    }

    public <T> Object asInfoObjectForPredifinedValues(T property) {
        if (property instanceof ListValue) {
            ListValue<IdWithNameValue> value = (ListValue<IdWithNameValue>) property;
            if (value.hasSingleValue()) {
                IdWithNameValue entry = value.getValue();
                return new IdWithNameInfo(entry.getId(), entry.getName());
            }
        }
        if (property instanceof IdWithNameValue) {
            return asInfo((IdWithNameValue)property);
        }
        return property;
    }

    private <T> Object asInfo(IdWithNameValue property) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = property.getId();
        info.name = property.getName();
        return info;
    }
}
