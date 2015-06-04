package com.elster.jupiter.search;

import com.elster.jupiter.domain.util.Finder;

import java.util.List;

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
    public String getId();

    /**
     * Tests if this SearchDomain supports
     * searches for the specified domain class.
     * In other words, tests if this SearchDomain
     * searches for objects of the specified java class.
     * When this SearchDomain represents a hierarchy
     * of domain classes then this will only return
     * <code>true</code> if the specified class
     * is the root of that class hierarchy.
     *
     * @return true iff this SearchDomain searches for objects of the specified java class
     */
    public boolean supports(Class domainClass);

    /**
     * Gets the List of {@link SearchableProperty}
     * that can be used to specify criteria
     * to search for values of this SearchDomain.
     *
     * @return The list of SearchableProperty
     */
    public List<SearchableProperty> getProperties();

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
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions);

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
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions);

}