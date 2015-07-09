package com.energyict.mdc.multisense.api.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/9/15.
 */
public class FieldSelector {
    public  <I, O> List<PropertyCopier<I, O>> getSelectedFields(Collection<String> fields, Map<String, PropertyCopier<I,O>> fieldMap) {
        if (fields==null || fields.isEmpty()) {
            fields = fieldMap.keySet();
        }
        return fields.stream().filter(fieldMap::containsKey).map(fieldMap::get).collect(toList());
    }



}
