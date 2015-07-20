package com.energyict.mdc.multisense.api.impl.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.QueryParam;

/**
 * Converts CSV string to list of individual field names
 */
public class FieldSelection {
    /**
     * Comma separated list of fields that will be added to the response,
     * if absent, all fields will be added.
     */
    private @QueryParam("fields") String fields;

    public List<String> getFields() {
        return fields!=null? Collections.unmodifiableList(Arrays.asList(fields.split(","))): Collections.emptyList();
    }
}
