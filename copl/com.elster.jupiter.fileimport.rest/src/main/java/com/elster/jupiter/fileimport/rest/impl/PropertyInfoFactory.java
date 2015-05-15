package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.ListValueEntry;
import com.elster.jupiter.rest.util.properties.ListValueInfo;

import java.util.ArrayList;
import java.util.List;

public class PropertyInfoFactory {

    public <T> Object asInfoObject(T property) {
        if (property instanceof ListValue) {
            ListValue<ListValueEntry> value = (ListValue<ListValueEntry>) property;
            List<String> infos = new ArrayList<>();
            for (ListValueEntry entry : value.getValues()) {
                infos.add(entry.getId());
            }
            return infos;
        }
        return property;
    }

    public <T> Object asInfoObjectForPredifinedValues(T property) {
        if (property instanceof ListValue) {
            ListValue<ListValueEntry> value = (ListValue<ListValueEntry>) property;
            if (value.hasSingleValue()) {
                ListValueEntry entry = value.getValue();
                return new ListValueInfo(entry.getId(), entry.getName());
            }
        }
        return property;
    }
}
