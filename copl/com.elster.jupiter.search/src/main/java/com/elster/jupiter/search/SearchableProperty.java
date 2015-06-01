package com.elster.jupiter.search;

import aQute.bnd.annotation.ProviderType;
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
 * <p>
 * Currently, the fact that a {@link PropertySpec} can be
 * "required" or "optional" is not.
 * A property's visibility is either "sticky" or "removable".
 * Sticky properties are intended to be always visible on the
 * search screen while "removable" ones may be removed by the
 * user when he no longer wants to search against that property.
 * </p>
 * <p>
 * A SearchableProperty can be part of a group and all
 * properties of the same group are expected to be rendered
 * together in the UI to indicate to the user that they are
 * somehow related. A group could e.g. be used to indicate
 * that the properties in the group actually belong to
 * a related object of the actual SearchDomain.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (14:34)
 */
@ProviderType
public interface SearchableProperty {

    /**
     * Models the visibility of a SearchableProperty.
     */
    public enum Visibility {
        /**
         * The SearchableProperty sticks on the screen
         * and is therefor always visible.
         */
        STICKY,

        /**
         * The SearchableProperty is not necessarily
         * always visible, it can be removed by the
         * user when no longer necessary.
         */
        REMOVABLE;
    }

    /**
     * Determines selection mode of the SearchableProperty.
     */
    public enum SelectionMode {
        /**
         * Supports only a single value to be specified/selected at the same time.
         */
        SINGLE,

        /**
         * Supports multiple values to be specified/selected at the same time.
         */
        MULTI;
    }

    public SearchDomain getDomain();

    public Optional<SearchablePropertyGroup> getGroup();

    public PropertySpec getSpecification();

    public Visibility getVisibility();

    public SelectionMode getSelectionMode();

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