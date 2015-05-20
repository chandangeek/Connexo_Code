package com.energyict.mdc.multisense.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.QueryParam;

/**
 * Created by bvn on 5/13/15.
 */
public class FieldList {

    private final List<String> split;

    public FieldList(@QueryParam("fields") String fields) {
        split = fields!=null? Collections.unmodifiableList(Arrays.asList(fields.split(","))): Collections.emptyList();
    }

    public List<String> getFields() {
        return split;
    }
}
