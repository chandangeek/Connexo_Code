/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.units.Quantity;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
                path(DynamicSearchResource.class, property.getName().equals("location") ? "getLocationFullCriteriaInfo" : "getFullCriteriaInfo").
                resolveTemplate("domain", property.getDomain().getId()).
                resolveTemplate("property", property.getName()).
                build();
    }


    public PropertyInfo asSingleObject(SearchableProperty property, UriInfo uriInfo, String nameFilter) {
        PropertyInfo propertyInfo = asListObject(property, uriInfo);
        PropertySpec propertySpec = property.getSpecification();
        propertyInfo.type = propertySpec.getValueFactory().getValueType().getSimpleName();
        propertyInfo.factoryName = propertySpec.getValueFactory().getClass().getName();

        if (propertySpec.getValueFactory() instanceof QuantityValueFactory) {
            PropertySpecPossibleValues possibleValuesOrNull = propertySpec.getPossibleValues();
            if (possibleValuesOrNull != null) {
                List<?> possibleValues = possibleValuesOrNull.getAllValues();
                propertyInfo.exhaustive = possibleValuesOrNull.isExhaustive();
                propertyInfo.values = possibleValues.stream()
                        .map(v -> asJsonValueObject(property.toDisplay(v), v))
                        .filter(getNameFilter(nameFilter))
                        .collect(toList());
                propertyInfo.total = propertyInfo.values != null ? propertyInfo.values.size() : 0;
                return propertyInfo;
            }
        }

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

    public PropertyInfo asSingleObjectWithValues(SearchableProperty property, UriInfo uriInfo, Map<Long, String> locations) {
        PropertyInfo propertyInfo = asListObject(property, uriInfo);
        PropertySpec propertySpec = property.getSpecification();
        propertyInfo.type = propertySpec.getValueFactory().getValueType().getSimpleName();
        propertyInfo.factoryName = propertySpec.getValueFactory().getClass().getName();
        propertyInfo.values = this.getLocationValues(locations);
        propertyInfo.exhaustive = true;
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

    public static IdWithDisplayValueInfo asJsonValueObject(String name, Object valueObject) {
        IdWithDisplayValueInfo info = new IdWithDisplayValueInfo();

        if (valueObject instanceof Quantity) {
            class QuantityInfo {
                public String value;
                public Integer multiplier;
                public String unit;
                public String displayValue;
            }
            QuantityInfo quantityInfo = new QuantityInfo();
            QuantityValueFactory quantityValueFactory = new QuantityValueFactory();
            Quantity value = (Quantity) valueObject;
            quantityInfo.value = value.getValue().toPlainString();
            quantityInfo.multiplier = value.getMultiplier();
            quantityInfo.unit = value.getUnit().toString();
            String[] valueParts = value.toString(false).split(" ");
            quantityInfo.displayValue = valueParts[1];
            info.id = quantityValueFactory.toStringValue(value);
            info.displayValue = quantityInfo.displayValue;
            return info;
        }

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

    private List<IdWithDisplayValueInfo> getLocationValues(Map<Long, String> locations) {
        List<IdWithDisplayValueInfo> list = locations.entrySet().
                stream().
                map(l -> {
                    IdWithDisplayValueInfo location = new IdWithDisplayValueInfo();
                    location.id = l.getKey();
                    location.displayValue = l.getValue();
                    return location;
                }).
                collect(Collectors.toList());

        return list;
    }

}
