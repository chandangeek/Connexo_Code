package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.search.SearchableProperty;
import java.util.List;
import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 6/1/15.
 */
public class PropertyInfo {
    public String name; // the property's name: use this to communicate filter values to the server
    public String displayValue; // The value to use as label in the UI
    public IdWithDisplayValueInfo group; // Identifies in which group this property should be listed; can be null;
    public String type; // Identifies the type of property: String, Integer, Date, ...
    public boolean exhaustive; // 'true' indicates UI can obtain an exhaustive list of values from which to select value(s)
    public boolean affectsAvailableDomainProperties; // true if using this property(with values) as filter will impact the available properties of a search domain
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public Link link;
    @XmlJavaTypeAdapter(SelectionModeAdapter.class)
    public SearchableProperty.SelectionMode selectionMode; // Indicates if only a single or multiple values can be used in the filter
    @XmlJavaTypeAdapter(VisibilityAdapter.class)
    public SearchableProperty.Visibility visibility; // Indicates if the property should always be displayed as filter property (sticky) or is removable
    public List<String> constraints; // List of other properties who's value will be used to narrow down possible values for this property
}
