/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;


import com.elster.jupiter.search.SearchableProperty;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;

public class SearchCriterionInfoFactory {

    public PropertyInfo asListObject(SearchableProperty property, UriInfo uriInfo) {
        return new PropertyInfo(property).withLink(Link.fromUri(getCriteriaDetailsUri(property, uriInfo)).build());
    }

    private URI getCriteriaDetailsUri(SearchableProperty property, UriInfo uriInfo) {
        return uriInfo.
                getBaseUriBuilder().
                path(DynamicSearchResource.class).
                path(DynamicSearchResource.class, property.getName().equals("location") ? "getLocationFullCriteriaInfo" : "getFullCriteriaInfo").
                resolveTemplate("domain", property.getDomain().getId()).
                resolveTemplate("property", property.getName()).
                build();
    }

    public PropertyInfo asSingleObject(SearchableProperty property, UriInfo uriInfo, String displayValueFilter) {
        return new PropertyInfo(property).withLink(Link.fromUri(getCriteriaDetailsUri(property, uriInfo)).build())
                .withSpecDetails()
                .withPossibleValues(displayValueFilter);
    }

    public PropertyInfo asSingleObjectWithValues(SearchableProperty property, UriInfo uriInfo, Map<Long, String> locations) {
        return new PropertyInfo(property).withLink(Link.fromUri(getCriteriaDetailsUri(property, uriInfo)).build())
                .withSpecDetails()
                .withPossibleValues(locations);
    }

}
