/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.search.SearchableProperty;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import static com.elster.jupiter.search.SearchableProperty.SelectionMode.MULTI;
import static com.elster.jupiter.search.SearchableProperty.SelectionMode.SINGLE;

/**
 * Created by bvn on 6/1/15.
 */
public class SelectionModeAdapter extends XmlAdapter<String, SearchableProperty.SelectionMode> {

    @Override
    public SearchableProperty.SelectionMode unmarshal(String v) throws Exception {
        switch(v) {
            case "multiple": return MULTI;
            case "single":  return SINGLE;
            default: throw new WebApplicationException("Unsupported SelectionMode");
        }
    }

    @Override
    public String marshal(SearchableProperty.SelectionMode v) throws Exception {
        switch(v) {
            case MULTI: return "multiple";
            case SINGLE: return "single";
            default: throw new WebApplicationException("Unsupported SelectionMode");
        }
    }
}
