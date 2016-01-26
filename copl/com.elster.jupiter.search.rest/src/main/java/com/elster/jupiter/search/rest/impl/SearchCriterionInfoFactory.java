package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.util.HasId;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class SearchCriterionInfoFactory {

    public PropertyInfo asListObject(SearchableProperty property, UriInfo uriInfo) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.name = property.getName();
        propertyInfo.displayValue = property.getDisplayName();
        propertyInfo.affectsAvailableDomainProperties = property.affectsAvailableDomainProperties();
        if (property.getGroup().isPresent()) {
            propertyInfo.group = new IdWithDisplayValueInfo();
            propertyInfo.group.id = property.getGroup().get().getId();
            propertyInfo.group.displayValue = property.getGroup().get().getDisplayName();
        }
        propertyInfo.selectionMode = property.getSelectionMode();
        propertyInfo.visibility = property.getVisibility();
        propertyInfo.constraints = property.getConstraints().stream().map(c -> c.getName()).collect(toList());
        propertyInfo.link = Link.fromUri(getCriteriaDetailsUri(property, uriInfo)).build();
        return propertyInfo;
    }

    private URI getCriteriaDetailsUri(SearchableProperty property, UriInfo uriInfo) {
        return uriInfo.
                getBaseUriBuilder().
                path(DynamicSearchResource.class).
                path(DynamicSearchResource.class, "getFullCriteriaInfo").
                resolveTemplate("domain", property.getDomain().getId()).
                resolveTemplate("property", property.getName()).
                build();
    }


    public PropertyInfo asSingleObject(SearchableProperty property, UriInfo uriInfo, String nameFilter) {
        PropertyInfo propertyInfo = asListObject(property, uriInfo);
        PropertySpec propertySpec = property.getSpecification();
        propertyInfo.type = propertySpec.getValueFactory().getValueType().getSimpleName();
        propertyInfo.factoryName = propertySpec.getValueFactory().getClass().getName();
        PropertySpecPossibleValues possibleValuesOrNull = propertySpec.getPossibleValues();
        if (possibleValuesOrNull != null) {
            List<?> possibleValues = possibleValuesOrNull.getAllValues();
            propertyInfo.exhaustive = possibleValuesOrNull.isExhaustive();
            propertyInfo.values = possibleValues.stream()
                    .map(v -> asJsonValueObject(property.toDisplay(v), v))
                    .filter(getNameFilter(nameFilter))
                    .sorted((v1, v2) -> v1.displayValue.compareToIgnoreCase(v2.displayValue))
                    .collect(toList());
        }
        propertyInfo.total = propertyInfo.values != null ? propertyInfo.values.size() : 0;
        return propertyInfo;
    }

    private Predicate<IdWithDisplayValueInfo> getNameFilter(String nameFilter) {
        Predicate<IdWithDisplayValueInfo> nameFilterPredicate;
        if (nameFilter != null) {
            nameFilterPredicate = dv -> dv.displayValue.toLowerCase().contains(nameFilter.toLowerCase());
        } else {
            nameFilterPredicate = dv -> true;
        }
        return nameFilterPredicate;
    }

    private IdWithDisplayValueInfo asJsonValueObject(String name, Object valueObject) {
        IdWithDisplayValueInfo info = new IdWithDisplayValueInfo();
        info.displayValue = name;
        info.id = name; // support for dynamic attributes, whose possible values don't match these classes
        if (HasId.class.isAssignableFrom(valueObject.getClass())) {
            info.id = ((HasId) valueObject).getId();
        } else if (Enum.class.isAssignableFrom(valueObject.getClass())) {
            info.id = ((Enum) valueObject).name();
        } else if (Long.class.isAssignableFrom(valueObject.getClass())) {
            info.id = valueObject;
        } else if (HasIdAndName.class.isAssignableFrom(valueObject.getClass())) {
            info.id = ((HasIdAndName) valueObject).getId();
        }
        return info;
    }

}
