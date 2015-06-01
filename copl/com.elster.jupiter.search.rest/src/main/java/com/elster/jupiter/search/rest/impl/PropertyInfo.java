package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.search.SearchableProperty;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 6/1/15.
 */
public class PropertyInfo {
    public String name;
    public String group;
    public String type;
    @XmlJavaTypeAdapter(SelectionModeAdapter.class)
    public SearchableProperty.SelectionMode selectionMode;
    @XmlJavaTypeAdapter(VisibilityAdapter.class)
    public SearchableProperty.Visibility visibility;
    public List<String> constraints;
}
