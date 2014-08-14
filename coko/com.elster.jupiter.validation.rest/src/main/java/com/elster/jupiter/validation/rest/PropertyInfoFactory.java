package com.elster.jupiter.validation.rest;

import com.elster.jupiter.properties.ListValueEntry;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.rest.util.properties.ListValueInfo;

public class PropertyInfoFactory {

    public <T> Object asInfoObject(T property) {
        if (property instanceof ListValue) {
            ListValue<ListValueEntry> value = (ListValue<ListValueEntry>) property;
            if (value.hasSingleValue()) {
                return new ListValueInfo(value.getValue().getId(), value.getValue().getName());
            }
            ListValueInfo[] infos = new ListValueInfo[value.getValues().size()];
            int i = 0;
            for (ListValueEntry obj : value.getValues()) {
                infos[i++] = new ListValueInfo(obj.getId(), obj.getName());
            }
            return infos;
        }
        return property;
    }
}
