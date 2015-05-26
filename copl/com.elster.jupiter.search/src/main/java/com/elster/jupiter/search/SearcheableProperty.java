package com.elster.jupiter.search;

import com.elster.jupiter.properties.PropertySpec;

/**
 * Models a property of a {@link SearchDomain domain class}
 * that can be used for searching.
 * In other words, client code can use
 * a SearcheableProperty to specify a query
 * that returns values of the SearchDomain
 * whose property is equal to a specified value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (14:34)
 */
public interface SearcheableProperty {

    public SearchDomain getDomain();

    public PropertySpec getSpecification();

}