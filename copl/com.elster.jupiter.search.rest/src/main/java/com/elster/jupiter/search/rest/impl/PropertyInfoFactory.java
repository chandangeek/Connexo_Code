package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.search.SearchableProperty;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 6/1/15.
 */
public class PropertyInfoFactory {
    public PropertyInfo asInfoObject(SearchableProperty property) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.name = property.getSpecification().getName();
        propertyInfo.type = property.getSpecification().getValueFactory().getValueType().getSimpleName();
        property.getGroup().ifPresent(g->propertyInfo.group=g.getId());
        propertyInfo.selectionMode = property.getSelectionMode();
        propertyInfo.visibility = property.getVisibility();
        propertyInfo.constraints = property.getConstraints().stream().map(c->c.getSpecification().getName()).collect(toList());

        return propertyInfo;
    }

}
