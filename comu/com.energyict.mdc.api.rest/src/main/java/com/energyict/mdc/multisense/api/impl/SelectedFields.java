package com.energyict.mdc.multisense.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.QueryParam;

/**
 * Converts CSV string to list of individual field names
 */
public class SelectedFields {

    private final List<String> split;

    public SelectedFields(@QueryParam("fields") String fields) {
        split = fields!=null? Collections.unmodifiableList(Arrays.asList(fields.split(","))): Collections.emptyList();
    }

    public List<String> getFields() {
        return split;
    }
}
