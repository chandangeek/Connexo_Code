package com.elster.jupiter.search;

import com.elster.jupiter.properties.PropertySpec;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Models a property of a {@link SearchDomain domain class}
 * that can be used for searching.
 * In other words, client code can use
 * a SearchableProperty to specify a query
 * that returns values of the SearchDomain
 * whose property is equal to a specified value.
 * <p>
 * Values of a SearchableProperty can be dependent on the
 * value of another SearchableProperty.
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
     * Gets the parent SearchableProperty on which values
     * of this SearchableProperty are dependent.
     * Referring back to the animal example used
     * in the class javadoc, the "subclass" SearchDomain
     * would return the "class" SearchDomain and the
     * "class" SearchDomain would return Optional.empty().
     *
     * @return The parent SearchDomain
     */
    public Optional<SearchableProperty> getParent();

    /**
     * Refreshes this SearchableProperty after the specified
     * values for the parent SearchableProperty was selected.
     * This will typically refresh the possible values
     * of the {@link PropertySpec}.
     *
     * @param parentValues The values for the parent SearchableProperty
     */
    public void refreshWithParents(List<Object> parentValues);

}