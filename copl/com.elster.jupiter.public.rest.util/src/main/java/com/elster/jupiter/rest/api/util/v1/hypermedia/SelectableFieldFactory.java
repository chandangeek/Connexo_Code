/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.hypermedia;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by bvn on 7/9/15.
 */
public abstract class SelectableFieldFactory<I, B> {

    /**
     * Helper method that calculates and copies selected fields from the domain object to the info object
     *
     * @param infoObject Copy destination
     * @param businessObject Copy source
     * @param uriInfo UriInfo as received from Jersey, required to build URIs
     * @param fields List of fields requested by the user. All fields in case list is empty.
     */
    protected final void copySelectedFields(I infoObject, B businessObject, UriInfo uriInfo, Collection<String> fields) {
        Map<String, PropertyCopier<I, B>> fieldMap = buildFieldMap();
        if (fields == null || fields.isEmpty()) {
            fields = fieldMap.keySet();
        }
        fields.stream()
                .filter(fieldMap::containsKey)
                .map(fieldMap::get)
                .forEach(copier -> copier.copy(infoObject, businessObject, uriInfo));
    }

    /**
     * Generic helper method, list all known fields on the resource
     */
    public final Set<String> getAvailableFields() {
        return buildFieldMap().keySet();
    }


    /**
     * Returns a map for field name -> method to copy the fields value to the info object (cfr Supplier), thus delaying value calculation
     * up to the point that the value is actually required.
     *
     * @return map field name -> propertyCopier
     */
    abstract protected Map<String, PropertyCopier<I, B>> buildFieldMap();


}
