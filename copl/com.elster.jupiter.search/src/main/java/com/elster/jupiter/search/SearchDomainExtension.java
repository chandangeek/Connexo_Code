/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

import com.elster.jupiter.util.sql.SqlFragment;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * A {@link SearchDomainExtension} enables to add {@link SearchableProperty}s and
 * {@link SearchablePropertyGroup}s to a specific {@link SearchDomain}
 */
@ProviderType
public interface SearchDomainExtension {

    /**
     * Tests if this SearchDomainExtension extends the given SearchDomain
     * when the {@link SearchablePropertyConstriction}s are applied.
     * An extension is meant to add additional SearchProperties to a SearchDomain
     *
     * @return true if this SearchDomainExtension extends the given one
     */
    boolean isExtensionFor(SearchDomain domain, List<SearchablePropertyConstriction> constrictions);

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
     * that are known to affect the available properties
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
    default List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        return getProperties();
    }

    SqlFragment asFragment(List<SearchablePropertyCondition> conditions);
}
