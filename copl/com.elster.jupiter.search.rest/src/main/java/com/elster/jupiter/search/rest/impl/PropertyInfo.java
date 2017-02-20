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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PropertyInfo {

    public String name; // the property's name: use this to communicate filter values to the server
    public String displayValue; // The value to use as label in the UI
    public IdWithDisplayValueInfo group; // Identifies in which group this property should be listed; can be null;
    public String type; // Identifies the type of property: String, Integer, Date, ...
    public String factoryName; // Identifies the type of factory: BooleanFactory, DateFactory, DateAndTimeFactory, ...
    public boolean allowsIsDefined; // True if a criteria 'xxx is defined' is available for this property, false if not available(=default)
    public boolean allowsIsUndefined; //True is a criteria 'xxx' is undefined' is available for this property, false if not available (=default)
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

    @JsonIgnore
    private SearchableProperty property;
    @JsonIgnore
    private PropertySpec propertySpec;

    // Marshalling
    public PropertyInfo() {
    }

    public PropertyInfo(SearchableProperty property) {
        if (property == null) {
            throw new IllegalArgumentException("'property' cannot be null");
        }
        this.property = property;
        this.name = property.getName();
        this.displayValue = property.getDisplayName();
        this.affectsAvailableDomainProperties = property.affectsAvailableDomainProperties();
        if (property.getGroup().isPresent()) {
            this.group = new IdWithDisplayValueInfo<>(property.getGroup().get().getId(), property.getGroup().get().getDisplayName());
        }
        this.selectionMode = property.getSelectionMode();
        this.visibility = property.getVisibility();
        this.constraints = property.getConstraints().stream().map(SearchableProperty::getName).collect(toList());
        this.allowsIsDefined = property.allowsIsDefined();
        this.allowsIsUndefined = property.allowsIsUnDefined();
    }

    PropertyInfo withLink(Link link) {
        this.link = link;
        return this;
    }

    protected PropertyInfo withSpecDetails() {
        PropertySpec spec = getPropertySpec().orElseThrow(() -> new IllegalStateException("SearchProperty is not set."));
        this.type = spec.getValueFactory().getValueType().getSimpleName();
        this.factoryName = spec.getValueFactory().getClass().getName();
        return this;
    }

    protected PropertyInfo withPossibleValues() {
        PropertySpec spec = getPropertySpec().orElseThrow(() -> new IllegalStateException("SearchProperty is not set."));
        PropertySpecPossibleValues possible = spec.getPossibleValues();
        if (possible != null) {
            this.values = possibleValuesAsStream(possible.getAllValues()).sorted((v1, v2) -> v1.displayValue.compareToIgnoreCase(v2.displayValue)).collect(Collectors.toList());
        }
        return this;
    }

    protected PropertyInfo withPossibleValues(String displayValueFilter) {
        PropertySpec spec = getPropertySpec().orElseThrow(() -> new IllegalStateException("SearchProperty is not set."));
        PropertySpecPossibleValues possible = spec.getPossibleValues();
        if (possible != null) {
            this.values = new ArrayList<>();
            this.exhaustive = possible.isExhaustive();
            Stream<IdWithDisplayValueInfo> valueInfoStream;
            Predicate<IdWithDisplayValueInfo> displayValueFilterFilter = getDisplayValueFilter(displayValueFilter);
            if (spec.getValueFactory() instanceof QuantityValueFactory) {
                valueInfoStream = possibleQuantitiesAsStream((QuantityValueFactory) spec.getValueFactory(), possible.getAllValues()).filter(displayValueFilterFilter);
            } else {
                valueInfoStream = possibleValuesAsStream(possible.getAllValues()).filter(displayValueFilterFilter).sorted((v1, v2) -> v1.displayValue.compareToIgnoreCase(v2.displayValue));
            }

            this.values.addAll(valueInfoStream.collect(Collectors.toList()));
        }
        return this;
    }

    protected PropertyInfo withPossibleValues(Map<?, String> possibleValueMap) {
        this.values = possibleValueMap.entrySet().stream().map(v -> new IdWithDisplayValueInfo<>(v.getKey(), v.getValue())).collect(Collectors.toList());
        return this;
    }

    @JsonGetter
    public Integer getTotal() {
        if (this.values != null) {
            return values.size();
        }
        return null;
    }

    private Optional<PropertySpec> getPropertySpec() {
        if (this.property != null) {
            if (this.propertySpec == null) {
                this.propertySpec = this.property.getSpecification();
            }
        }
        return Optional.ofNullable(this.propertySpec);
    }

    private Stream<IdWithDisplayValueInfo> possibleQuantitiesAsStream(final QuantityValueFactory factory, List<Quantity> quantities) {
        return quantities.stream().map((q) -> new IdWithDisplayValueInfo<>(factory.toStringValue(q), q.toString(false).split(" ")[1]));
    }

    private Stream<IdWithDisplayValueInfo> possibleValuesAsStream(List<?> possibleValues) {
        return possibleValues.stream().map((pv) -> new IdWithDisplayValueInfo<>(idFor(pv), this.property.toDisplay(pv)));
    }

    private Object idFor(Object value) {
        if (HasId.class.isAssignableFrom(value.getClass())) {
            return ((HasId) value).getId();
        } else if (Enum.class.isAssignableFrom(value.getClass())) {
            return ((Enum) value).name();
        } else if (Long.class.isAssignableFrom(value.getClass())) {
            return value;
        } else if (HasIdAndName.class.isAssignableFrom(value.getClass())) {
            return ((HasIdAndName) value).getId();
        }
        return this.property.toDisplay(value); // support for dynamic attributes, whose possible values don't match these classes
    }

    private Predicate<IdWithDisplayValueInfo> getDisplayValueFilter(String displayValueFilter) {
        Predicate<IdWithDisplayValueInfo> displayValueFilterPredicate;
        if (displayValueFilter != null) {
            displayValueFilterPredicate = dv -> dv.displayValue.toLowerCase().contains(displayValueFilter.toLowerCase());
        } else {
            displayValueFilterPredicate = dv -> true;
        }
        return displayValueFilterPredicate;
    }

}
