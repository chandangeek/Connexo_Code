/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-03 (09:03)
 */
@Component(name = "com.elster.jupiter.search.gogo",
        service = SearchGogo.class,
        property = {
                "osgi.command.scope=jsm",
                "osgi.command.function=listDomains",
                "osgi.command.function=listProperties"},
        immediate = true)
@SuppressWarnings("unused")
public class SearchGogo {

    private volatile SearchService searchService;

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @SuppressWarnings("unused")
    public void listDomains() {
        this.searchService
                .getDomains()
                .stream()
                .map(SearchDomain::getId)
                .forEach(System.out::println);
    }

    @SuppressWarnings("unused")
    public void listProperties() {
        System.out.println("Usage jsm:listProperties <domain-id> | <domain-id> [<condition [, <condition>]*]");
        System.out.println("      where condition is: <key>=<value>");
        System.out.println("      and key is one of the properties of the domain that affects the properties");
        System.out.println("      and value is the String representation of the type of the key that was used");
    }

    @SuppressWarnings("unused")
    public void listProperties(String id) {
        Optional<SearchDomain> domain = this.searchService.findDomain(id);
        if (domain.isPresent()) {
            this.listProperties(domain.get());
        }
        else {
            System.out.println("Domain not found");
        }
    }

    private void listProperties(SearchDomain domain) {
        domain.getProperties().stream().forEach(this::print);
    }

    private void print(SearchableProperty property) {
        this.printNameAndType(property);
        System.out.println("\tUI name: " + property.getDisplayName());
        property.getGroup().ifPresent(this::printGroup);
        System.out.println("\t" + property.getVisibility());
        System.out.println("\t" + property.getSelectionMode());
        this.printConstraints(property);
    }

    private void printNameAndType(SearchableProperty property) {
        System.out.print(property.getName());
        System.out.println(property.getSpecification().getValueFactory().getValueType().getName());
    }

    private void printGroup(SearchablePropertyGroup searchablePropertyGroup) {
        System.out.println("\tin group: " + searchablePropertyGroup.getDisplayName());
    }

    private void printConstraints(SearchableProperty property) {
        List<SearchableProperty> constraints = property.getConstraints();
        if (constraints.isEmpty()) {
            System.out.println("\tno constraints");
        }
        else {
            System.out.println("\tconstraint by: " + constraints.stream().map(SearchableProperty::getName).collect(Collectors.joining(", ")));
        }
    }

    @SuppressWarnings("unused")
    public void listProperties(String id, String... conditions) {
        Optional<SearchDomain> domain = this.searchService.findDomain(id);
        if (domain.isPresent()) {
            this.listProperties(
                    domain.get(),
                    Stream
                        .of(conditions)
                        .map(c -> this.toConstriction(domain.get(), c))
                        .collect(Collectors.toList()));
        }
        else {
            System.out.println("Domain not found");
        }
    }

    private SearchablePropertyConstriction toConstriction(SearchDomain domain, String condition) {
        String[] keyAndValue = condition.split("=");
        if (keyAndValue.length == 2) {
            SearchableProperty property = domain.getProperties().stream().filter(p -> p.hasName(keyAndValue[0])).findAny().orElseThrow(() -> new IllegalArgumentException("Domain does not have property with name " + keyAndValue[0]));
            try {
                Object constrictingValue = property.getSpecification().getValueFactory().fromStringValue(keyAndValue[1]);
                if (constrictingValue == null) {
                    throw new IllegalArgumentException("Value '" + keyAndValue[1] + "' for property '" + property.getName() + "' is not valid");
                }
                return SearchablePropertyConstriction.withValues(property, constrictingValue);
            }
            catch (NoSuchElementException e) {
                throw new IllegalArgumentException("Value '" + keyAndValue[1] + "' for property '" + property.getName() + "' is not valid");
            }
        }
        else {
            throw new IllegalArgumentException("All key value conditions must be written as: <key>=<value>");
        }
    }

    private void listProperties(SearchDomain domain, List<SearchablePropertyConstriction> constrictions) {
        domain
            .getPropertiesWithConstrictions(this.addMissingConstrictions(domain, constrictions))
            .stream()
            .forEach(this::print);
    }

    private List<SearchablePropertyConstriction> addMissingConstrictions(SearchDomain domain, List<SearchablePropertyConstriction> incomplete) {
        Map<String, SearchablePropertyConstriction> allConstrictions =
                incomplete
                    .stream()
                    .collect(Collectors.toMap(
                            c -> c.getConstrainingProperty().getName(),
                            Function.identity()));
        domain
            .getProperties()
            .stream()
            .filter(SearchableProperty::affectsAvailableDomainProperties)
            .filter(sp -> !allConstrictions.keySet().contains(sp.getName()))
            .forEach(sp -> allConstrictions.put(sp.getName(), SearchablePropertyConstriction.noValues(sp)));
        return new ArrayList<>(allConstrictions.values());
    }

}