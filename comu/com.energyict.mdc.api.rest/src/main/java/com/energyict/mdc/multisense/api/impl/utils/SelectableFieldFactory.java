package com.energyict.mdc.multisense.api.impl.utils;

import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/9/15.
 */
public abstract class SelectableFieldFactory<I,B> {

    protected final void copySelectedFields(I infoObject, B businessObject, UriInfo uriInfo, Collection<String> fields) {
        Map<String, PropertyCopier<I, B>> fieldMap = buildFieldMap();
        if (fields==null || fields.isEmpty()) {
            fields = fieldMap.keySet();
        }
        fields.stream().filter(fieldMap::containsKey).map(fieldMap::get).forEach(copier -> copier.copy(infoObject, businessObject, uriInfo));
    }

    public final Set<String> getAvailableFields() {
        return buildFieldMap().keySet();
    }


    abstract protected Map<String, PropertyCopier<I, B>> buildFieldMap();


}
