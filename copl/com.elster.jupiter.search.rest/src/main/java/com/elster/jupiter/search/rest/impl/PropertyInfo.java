/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.search.SearchableProperty;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Created by bvn on 6/1/15.
 */
public class PropertyInfo {
    public String name; // the property's name: use this to communicate filter values to the server
    public String displayValue; // The value to use as label in the UI
    public IdWithDisplayValueInfo group; // Identifies in which group this property should be listed; can be null;
    public String type; // Identifies the type of property: String, Integer, Date, ...
    public String factoryName; // Identifies the type of factory: BooleanFactory, DateFactory, DateAndTimeFactory, ...
    public Boolean exhaustive; // 'true' indicates UI can obtain an exhaustive list of values from which to select value(s)
    public boolean affectsAvailableDomainProperties; // true if using this property(with values) as filter will impact the available properties of a search domain
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public Link link;
    @XmlJavaTypeAdapter(SelectionModeAdapter.class)
    public SearchableProperty.SelectionMode selectionMode; // Indicates if only a single or multiple values can be used in the filter
    @XmlJavaTypeAdapter(VisibilityAdapter.class)
    public SearchableProperty.Visibility visibility; // Indicates if the property should always be displayed as filter property (sticky) or is removable
    public List<String> constraints; // List of other properties who's value will be used to narrow down possible values for this property
    public List<IdWithDisplayValueInfo> values; // List of all available variants for this property
    public long total; // Size of values list, FE needs it
}
