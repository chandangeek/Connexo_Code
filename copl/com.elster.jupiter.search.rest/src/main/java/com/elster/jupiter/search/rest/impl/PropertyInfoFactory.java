package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchableProperty;
import java.net.URI;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 6/1/15.
 */
public class PropertyInfoFactory {


    public PropertyInfo asInfoObject(SearchableProperty property, UriInfo uriInfo) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.name = property.getName();
        propertyInfo.displayValue = property.getDisplayName();
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        propertyInfo.exhaustive = possibleValues.isExhaustive();
        propertyInfo.affectsAvailableDomainProperties = property.affectsAvailableDomainProperties();
        propertyInfo.type = property.getSpecification().getValueFactory().getValueType().getSimpleName();
        property.getGroup().ifPresent(g->propertyInfo.group=g.getId());
        propertyInfo.selectionMode = property.getSelectionMode();
        propertyInfo.visibility = property.getVisibility();
        propertyInfo.constraints = property.getConstraints().stream().map(c->c.getSpecification().getName()).collect(toList());
        URI uri = uriInfo.
                getBaseUriBuilder().
                path(DynamicSearchResource.class).
                path(DynamicSearchResource.class, "getDomainPropertyValues").
                resolveTemplate("domain", property.getDomain().getId()).
                resolveTemplate("property", property.getName()).
                build();
        propertyInfo.link = Link.fromUri(uri).build();

        return propertyInfo;
    }

}
