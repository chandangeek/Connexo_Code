/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

/**
 * Models the behavior of a component that decides if a {@link CustomPropertySet}
 * can be enabled as a {@link com.elster.jupiter.search.SearchDomainExtension}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-14 (10:33)
 */
@ConsumerType
public interface CustomPropertySetSearchEnabler {

    /**
     * Gets the domain class of the {@link CustomPropertySet}
     * for which this CustomPropertySetSearchEnabler can make decisions.
     * The returned class will be matched against the domain class of the CustomPropertySet
     * before {@link #enableWhen(CustomPropertySet, List)} is called so only
     * CustomPropertySet with the appropriate domain class are passed to the enableWhen method.
     *
     * @return The domain class of the CustomPropertySet for which this CustomPropertySetSearchEnabler can make decisions
     */
    Class getDomainClass();

    /**
     * Tests if the specified {@link CustomPropertySet} can be enabled
     * as a {@link com.elster.jupiter.search.SearchDomainExtension}
     * when the specified {@link SearchablePropertyConstriction}s are applied.
     *
     * @param constrictions The list of SearchablePropertyConstriction
     * @return A flag that indicates if the CustomPropertySet can be enabled as a SearchDomainExtension or not
     */
    boolean enableWhen(CustomPropertySet customPropertySet, List<SearchablePropertyConstriction> constrictions);

    List<SearchableProperty> getConstrainingProperties(CustomPropertySet customPropertySet, List<SearchablePropertyConstriction> constrictions);
}