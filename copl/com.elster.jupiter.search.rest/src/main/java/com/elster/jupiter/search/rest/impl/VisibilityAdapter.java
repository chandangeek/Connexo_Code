/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.search.SearchableProperty;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by bvn on 6/1/15.
 */
public class VisibilityAdapter extends XmlAdapter<String, SearchableProperty.Visibility> {

    @Override
    public SearchableProperty.Visibility unmarshal(String v) throws Exception {
        switch(v) {
            case "removable": return SearchableProperty.Visibility.REMOVABLE;
            case "sticky":  return SearchableProperty.Visibility.STICKY;
            default: throw new WebApplicationException("Unsupported Visibility");
        }
    }

    @Override
    public String marshal(SearchableProperty.Visibility v) throws Exception {
        switch(v) {
            case STICKY: return "sticky";
            case REMOVABLE: return "removable";
            default: throw new WebApplicationException("Unsupported Visibility");
        }
    }
}
