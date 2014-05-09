package com.energyict.mdc.common.rest;

import com.energyict.mdc.common.HasId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods
 */
public class IdListBuilder {

    public static <T extends HasId> Map<Long, T> asIdMap(Collection<T> identifiables) {
        Map<Long, T> map = new HashMap<>();
        for (T t : identifiables) {
            map.put(t.getId(), t);
        }
        return map;
    }



}
