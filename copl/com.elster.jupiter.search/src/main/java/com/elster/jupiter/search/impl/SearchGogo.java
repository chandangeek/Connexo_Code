package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        System.out.println("Specify the id of a search domain to print all properties of that search domain");
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
        System.out.println("\tUI name:" + property.getDisplayName());
        property.getGroup().ifPresent(this::printGroup);
        System.out.println("\t" + property.getVisibility());
        System.out.println("\t" + property.getSelectionMode());
        this.printConstraints(property);
    }

    private void printNameAndType(SearchableProperty property) {
        System.out.print(property.getName());
        if (property.getSpecification().getValueFactory().isReference()) {
            System.out.print(" reference to ");
        }
        else {
            System.out.print(" of type ");
        }
        System.out.println(property.getSpecification().getValueFactory().getValueType().getName());
    }

    private void printGroup(SearchablePropertyGroup searchablePropertyGroup) {
        System.out.println("\tin group " + searchablePropertyGroup.getId());
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

}