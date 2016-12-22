package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link SearchDomain} interface
 * that wraps an existing SearchDomain and extends it with the properties
 * of the {@link SearchDomainExtension}s that have been registered
 * against it with the {@link SearchServiceImpl}.
 */
public class ExtendedSearchDomain implements SearchDomain {

    private final OrmService ormService;
    private final SearchServiceImpl searchService;
    private final SearchDomain originalDomain;

    public ExtendedSearchDomain(OrmService ormService, SearchServiceImpl searchService, SearchDomain originalDomain) {
        this.ormService = ormService;
        this.searchService = searchService;
        this.originalDomain = originalDomain;
    }

    @Override
    public String getId() {
        return this.originalDomain.getId();
    }

    @Override
    public String displayName() {
        return this.originalDomain.displayName();
    }

    @Override
    public List<String> targetApplications() {
        return this.originalDomain.targetApplications();
    }

    @Override
    public Class<?> getDomainClass() {
        return this.originalDomain.getDomainClass();
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return Stream.concat(
                this.originalDomain.getProperties().stream(),
                this.searchService.getSearchExtensions()
                        .stream()
                        .filter(extension -> extension.isExtensionFor(this.originalDomain, Collections.emptyList()))
                        .flatMap(extension -> extension.getProperties().stream().map(property -> new SearchDomainExtensionSearchableProperty(this, extension, property)))
        ).collect(Collectors.toList());
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        return Stream.concat(
                this.originalDomain.getPropertiesWithConstrictions(constrictions).stream(),
                this.searchService.getSearchExtensions()
                        .stream()
                        .filter(extension -> extension.isExtensionFor(this.originalDomain, constrictions))
                        .flatMap(extension -> extension.getPropertiesWithConstrictions(constrictions).stream().map(property -> new SearchDomainExtensionSearchableProperty(this, extension, property)))
        ).collect(Collectors.toList());
    }

    @Override
    public List<SearchablePropertyValue> getPropertiesValues(Function<SearchableProperty, SearchablePropertyValue> mapper) {
        // 1) retrieve all fixed search properties
        List<SearchableProperty> fixedProperties = getProperties();
        // 2) check properties which affect available domain properties
        List<SearchablePropertyConstriction> constrictions = fixedProperties.stream()
                .filter(SearchableProperty::affectsAvailableDomainProperties)
                .map(mapper::apply)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue.getValueBean().values != null)
                .map(SearchablePropertyValue::asConstriction)
                .collect(Collectors.toList());
        // 3) update list of available properties and convert these properties into properties values
        Map<String, SearchablePropertyValue> valuesMap = (constrictions.isEmpty() ? fixedProperties : getPropertiesWithConstrictions(constrictions))
                .stream()
                .map(mapper::apply)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue.getValueBean().values != null)
                .collect(Collectors.toMap(propertyValue -> propertyValue.getProperty().getName(), Function.identity(), (a, b) -> a));
        // 4) refresh all properties with their constrictions
        for (SearchablePropertyValue propertyValue : valuesMap.values()) {
            SearchableProperty property = propertyValue.getProperty();
            property.refreshWithConstrictions(property.getConstraints().stream()
                    .map(constrainingProperty -> valuesMap.get(constrainingProperty.getName()))
                    .filter(Objects::nonNull)
                    .map(SearchablePropertyValue::asConstriction)
                    .collect(Collectors.toList()));
        }
        return new ArrayList<>(valuesMap.values());
    }

    @Override
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions) {
        return SearchDomainExtensionSupportFinder.getFinder(this.ormService, this.originalDomain, conditions);
    }
}
