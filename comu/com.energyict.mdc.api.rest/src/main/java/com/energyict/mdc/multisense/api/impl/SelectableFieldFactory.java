package com.energyict.mdc.multisense.api.impl;

import java.util.Collection;
import java.util.Map;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/9/15.
 */
public interface SelectableFieldFactory<I,B> {

    default public void copySelectedFields(I infoObject, B businessObject, UriInfo uriInfo, Collection<String> fields) {
        Map<String, PropertyCopier<I, B>> fieldMap = buildFieldMap();
        if (fields==null || fields.isEmpty()) {
            fields = fieldMap.keySet();
        }
        fields.stream().filter(fieldMap::containsKey).map(fieldMap::get).forEach(copier -> copier.copy(infoObject, businessObject, uriInfo));
    }

    public Map<String, PropertyCopier<I, B>> buildFieldMap();


}
