/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

import com.elster.jupiter.domain.util.Finder;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Models a set of possible values for searching purposes.
 * It was named after the mathematical concept of a domain
 * that is defined as a set of possible values of the
 * independent variable or variables of a function.
 * Typically, a domain class or a hierarchy of domain classes
 * maps onto a single SearchDomain.
 * Note however that when a SearchDomain represents
 * a hierarchy of domain classes, it is currently
 * <strong>NOT</strong> supported to search
 * for instances of only a part of the domain hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (13:25)
 */
public interface SearchDomain {

    /**
     * Gets a unique identifier for this SearchDomain.
     *
     * @return The unique identifier
     */
    String getId();

    /**
     * Gets the name of this SearchDomain.
     * It is recommended to use a Thesaurus so that
     * the name is translated to the user's language.
     *
     * @return The name of this SearchDomain
     */
    String displayName();

    /**
     * Gets the list of applications for which
     * this SearchDomain was intended to be used
     * or an empty Collection if this SearchDomain
     * is intended to be used by all available applications.
     *
     * @return The List of target applications
     * @see SearchService#getDomains(String)
     */
    default List<String> targetApplications() {
        return Collections.emptyList();
    }

    /**
     * Gets the domain class that this SearchDomain is searching for.
     * When this SearchDomain represents a hierarchy of domain classes
     * then this return the root of that class hierarchy.
     * @return The domain class
     */
    Class<?> getDomainClass();

    /**
     * Gets the List of {@link SearchableProperty}
     * that can be used to specify criteria
     * to search for values of this SearchDomain.
     *
     * @return The list of SearchableProperty
     */
    List<SearchableProperty> getProperties();

    /**
     * Gets the List of {@link SearchableProperty}
     * that can be used to specify criteria
     * to search for values of this SearchDomain
     * after values have been selected for properties
     * that are know to affect the available properties
     * of this SearchDomain.
     * <p>
     * Note that implementation classes may throw an IllegalArgumentException
     * if not all properties that affect the available properties are included
     * in the List of {@link SearchablePropertyConstriction}s.
     * Note that implementation classes may also throw an IllegalArgumentException
     * if a SearchablePropertyConstriction is not marked as affecting the properties.
     * Note that implementation classes may also throw an IllegalArgumentException
     * if the constraining values of a SearchablePropertyConstriction
     * are not compatible with the specification of the constraining SearchableProperty.
     * </p>
     *
     * @param constrictions The List of SearchablePropertyConstriction
     * @return The list of SearchableProperty
     */
    List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions);

    List<SearchablePropertyValue> getPropertiesValues(Function<SearchableProperty, SearchablePropertyValue> mapper);

    /**
     * Creates a Finder for instances of this SearchDomain
     * for the specified {@link SearchablePropertyCondition}s.
     * Will throw an IllegalArgumentException when
     * at least one of the conditions is expressed
     * against a {@link SearchableProperty} that was
     * not produced by this SearchDomain.
     *
     * @param conditions The condition
     * @return The Finder
     * @throws IllegalArgumentException
     */
    Finder<?> finderFor(List<SearchablePropertyCondition> conditions);

}