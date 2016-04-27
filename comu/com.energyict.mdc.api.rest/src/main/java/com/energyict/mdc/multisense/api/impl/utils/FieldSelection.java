package com.energyict.mdc.multisense.api.impl.utils;

import javax.ws.rs.QueryParam;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Converts CSV string (as received from client) to list of individual field names
 */
public class FieldSelection {
    /**
     * @summary Comma separated list of fields that will be added to the response, if absent, all fields will be added.
     * Get a list of all available field with PROPFIND method on this resource.
     */
    private @QueryParam("fields") String fields;

    public List<String> getFields() {
        return fields!=null? Collections.unmodifiableList(Arrays.asList(fields.split(","))): Collections.emptyList();
    }
}
