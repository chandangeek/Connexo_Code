package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchableProperty;
import java.net.URI;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 6/1/15.
 */
public class SearchCriterionInfoFactory {


    public PropertyInfo asInfoObject(SearchableProperty property, UriInfo uriInfo) {
        PropertyInfo propertyInfo = new PropertyInfo();
        PropertySpec propertySpec = property.getSpecification();
        propertyInfo.name = property.getName();
        propertyInfo.displayValue = property.getDisplayName();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        propertyInfo.exhaustive = possibleValues!=null && possibleValues.isExhaustive();
        propertyInfo.affectsAvailableDomainProperties = property.affectsAvailableDomainProperties();
        propertyInfo.type = propertySpec.getValueFactory().getValueType().getSimpleName();
        propertyInfo.factoryName = propertySpec.getValueFactory().getClass().getName();
        if (property.getGroup().isPresent()) {
            propertyInfo.group=new IdWithDisplayValueInfo();
            propertyInfo.group.id = property.getGroup().get().getId();
            propertyInfo.group.displayValue = property.getGroup().get().getDisplayName();
        };
        propertyInfo.selectionMode = property.getSelectionMode();
        propertyInfo.visibility = property.getVisibility();
        propertyInfo.constraints = property.getConstraints().stream().map(c -> c.getName()).collect(toList());
        if (propertyInfo.exhaustive) {
            URI uri = uriInfo.
                    getBaseUriBuilder().
                    path(DynamicSearchResource.class).
                    path(DynamicSearchResource.class, "getDomainPropertyValues").
                    resolveTemplate("domain", property.getDomain().getId()).
                    resolveTemplate("property", property.getName()).
                    build();
            propertyInfo.link = Link.fromUri(uri).build();
        }

        return propertyInfo;
    }

}
