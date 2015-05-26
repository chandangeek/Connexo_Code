package com.elster.jupiter.search;

import java.util.List;
import java.util.Optional;

/**
 * Models a set of possible values for searching purposes.
 * It was named after the mathematical concept of a domain
 * that is defined as a set of possible values of the
 * independent variable or variables of a function.
 * Values of a SearchDomain can be dependent on the
 * value of another SearchDomain.
 * Consider the animal life on our planet as an
 * example of a SearchDomain. It could have "class" and "subclass"
 * as a SearcheableProperty. When the user selects
 * value "Vertebrates" in UI as possible value to filter on
 * then the list of possible values for "subclass"
 * will need to be updated. Also, as long as the user
 * has not made any specification on the "class"
 * it is not possible for the UI to compile a
 * list of "subclasses".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (13:25)
 */
public interface SearchDomain {

    public SearchProvider getProvider();

    /**
     * Gets the List of {@link SearcheableProperty}
     * that can be used to specify criteria
     * to search for values of this SearchDomain.
     *
     * @return The list of SearcheableProperty
     */
    public List<SearcheableProperty> getProperties();

    /**
     * Gets the parent SearchDomain on which values
     * of this SearchDomain are dependent.
     * Referring back to the animal example used
     * in the class javadoc, the "subclass" SearchDomain
     * would return the "class" SearchDomain and the
     * "class" SearchDomain would return Optional.empty().
     *
     * @return The parent SearchDomain
     */
    public Optional<SearchDomain> getParent();

}