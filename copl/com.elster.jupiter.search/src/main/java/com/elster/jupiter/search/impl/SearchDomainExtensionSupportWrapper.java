package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    public boolean supports(Class domainClass) {
        return originalDomain.supports(domainClass);
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
                    .flatMap(extension -> extension.getProperties().stream())
                    .map(property -> new DomainSearchableProperty(this, property))
                    .forEach(allProperties::add);
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
                    .flatMap(extension -> extension.getPropertiesWithConstrictions(constrictions).stream())
                    .map(property -> new DomainSearchableProperty(this, property))
                    .forEach(allProperties::add);
            return allProperties;
        }
        return this.originalDomain.getPropertiesWithConstrictions(constrictions);
    }

    @Override
    public List<SearchablePropertyValue> getPropertiesValues(Function<SearchableProperty, SearchablePropertyValue> mapper) {
        return originalDomain.getPropertiesValues(mapper);
    }

    @Override
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions) {
        return originalDomain.finderFor(conditions);
    }

    private static class DomainSearchableProperty implements SearchableProperty {
        private final SearchDomain searchDomain;
        private final SearchableProperty searchableProperty;

        public DomainSearchableProperty(SearchDomain searchDomain, SearchableProperty searchableProperty) {
            this.searchDomain = searchDomain;
            this.searchableProperty = searchableProperty;
        }

        @Override
        public SearchDomain getDomain() {
            return searchDomain;
        }

        @Override
        public boolean affectsAvailableDomainProperties() {
            return searchableProperty.affectsAvailableDomainProperties();
        }

        @Override
        public Optional<SearchablePropertyGroup> getGroup() {
            return searchableProperty.getGroup();
        }

        @Override
        public PropertySpec getSpecification() {
            return searchableProperty.getSpecification();
        }

        @Override
        public Visibility getVisibility() {
            return searchableProperty.getVisibility();
        }

        @Override
        public SelectionMode getSelectionMode() {
            return searchableProperty.getSelectionMode();
        }

        @Override
        public String getName() {
            return searchableProperty.getName();
        }

        @Override
        public String getDisplayName() {
            return searchableProperty.getDisplayName();
        }

        @Override
        public String toDisplay(Object value) {
            return searchableProperty.toDisplay(value);
        }

        @Override
        public boolean hasName(String name) {
            return searchableProperty.hasName(name);
        }

        @Override
        public List<SearchableProperty> getConstraints() {
            return searchableProperty.getConstraints();
        }

        @Override
        public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
            searchableProperty.refreshWithConstrictions(constrictions);
        }
    }
}
