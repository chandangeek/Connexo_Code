package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchDomainExtensionSupportWrapper implements SearchDomain {

    private final SearchServiceImpl searchService;
    private final SearchDomain originalDomain;

    public SearchDomainExtensionSupportWrapper(SearchServiceImpl searchService, SearchDomain originalDomain) {
        this.searchService = searchService;
        this.originalDomain = originalDomain;
    }

    @Override
    public String getId() {
        return originalDomain.getId();
    }

    @Override
    public String displayName() {
        return originalDomain.displayName();
    }

    @Override
    public List<String> targetApplications() {
        return originalDomain.targetApplications();
    }

    @Override
    public Class<?> getDomainClass() {
        return originalDomain.getDomainClass();
    }

    @Override
    public List<SearchableProperty> getProperties() {
        List<SearchDomainExtension> extensions = this.searchService.getSearchExtensions()
                .stream()
                .filter(extension -> extension.isExtensionFor(this.originalDomain, Collections.emptyList()))
                .collect(Collectors.toList());
        if (!extensions.isEmpty()) {
            List<SearchableProperty> allProperties = new ArrayList<>(this.originalDomain.getProperties());
            extensions.stream()
                    .forEach(extension -> extension.getProperties()
                            .stream()
                            .forEach(property -> allProperties.add(new SearchDomainExtensionSearchableProperty(this, extension, property))));
            return allProperties;
        }
        return this.originalDomain.getProperties();
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        List<SearchDomainExtension> extensions = this.searchService.getSearchExtensions()
                .stream()
                .filter(extension -> extension.isExtensionFor(this.originalDomain, constrictions))
                .collect(Collectors.toList());
        if (!extensions.isEmpty()) {
            List<SearchableProperty> allProperties = new ArrayList<>(this.originalDomain.getPropertiesWithConstrictions(constrictions));
            extensions.stream()
                    .forEach(extension -> extension.getPropertiesWithConstrictions(constrictions)
                            .stream()
                            .forEach(property -> allProperties.add(new SearchDomainExtensionSearchableProperty(this, extension, property))));
            return allProperties;
        }
        return this.originalDomain.getPropertiesWithConstrictions(constrictions);
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
                .collect(Collectors.toMap(propertyValue -> propertyValue.getProperty().getName(), Function.identity()));
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
        List<SearchablePropertyCondition> domainConditions = new ArrayList<>(conditions);
        Map<SearchDomainExtension, List<SearchablePropertyCondition>> extensioneConditions = new HashMap<>();
        conditions.stream()
                .filter(condition -> condition.getProperty() instanceof SearchDomainExtensionSearchableProperty)
                .forEach(condition -> {
                    domainConditions.remove(condition);
                    SearchDomainExtension domainExtension = ((SearchDomainExtensionSearchableProperty) condition.getProperty()).getDomainExtension();
                    List<SearchablePropertyCondition> singleExtensionConditions = extensioneConditions.get(domainExtension);
                    if (singleExtensionConditions == null) {
                        singleExtensionConditions = new ArrayList<>();
                        extensioneConditions.put(domainExtension, singleExtensionConditions);
                    }
                    singleExtensionConditions.add(condition);
                });
        if (extensioneConditions.isEmpty()) {
            return originalDomain.finderFor(domainConditions);
        }
        return new SearchDomainExtensionSupportFinder<>(originalDomain.finderFor(domainConditions), extensioneConditions);
    }
}
