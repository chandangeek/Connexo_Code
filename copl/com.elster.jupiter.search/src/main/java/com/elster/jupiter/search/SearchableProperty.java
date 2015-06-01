package com.elster.jupiter.search;

import com.elster.jupiter.properties.PropertySpec;

import java.util.List;
import java.util.Objects;

/**
 * Models a property of a {@link SearchDomain domain class}
 * that can be used for searching.
 * In other words, client code can use
 * a SearchableProperty to specify a query
 * that returns values of the SearchDomain
 * whose property is equal to a specified value.
 * <p>
 * Values of a SearchableProperty can be constraint by the
 * value of other properties.
 * Consider the animal life on our planet as an
 * example of a SearchDomain.
 * It could have "class" and "subclass" as a SearchableProperty.
 * When the user selects value "Vertebrates" as possible value
 * to search on then the list of possible values for "subclass"
 * will need to be updated. Also, as long as the user
 * has not made any specification on the "class"
 * it is not possible for the UI to compile a
 * list of "subclasses".
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (14:34)
 */
public interface SearchableProperty {

    public SearchDomain getDomain();

    public PropertySpec getSpecification();

    /**
     * Tests if the {@link PropertySpec specification}
     * of this SearchableProperty has the specified name.
     *
     * @param name The name
     * @return <code>true</code> iff the name of the PropertySpec equals the specified name
     */
    public default boolean hasName(String name) {
        Objects.requireNonNull(name);
        return name.equals(getSpecification().getName());
    }

    /**
     * Gets the List of SearchableProperty that put
     * constraints on the values of this SearchableProperty.
     * Referring back to the animal example used
     * in the class javadoc, the "subclass" SearchDomain
     * would return the "class" SearchDomain and the
     * "class" SearchDomain would return an empty List.
     *
     * @return The parent SearchDomain
     */
    public List<SearchableProperty> getConstraints();

    /**
     * Refreshes this SearchableProperty after the specified
     * values for one of the constraining SearchableProperty was selected.
     * This will typically refresh the possible values
     * of the {@link PropertySpec}.
     *
     * @param constrictions The {@link SearchablePropertyConstriction}s
     */
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions);

}