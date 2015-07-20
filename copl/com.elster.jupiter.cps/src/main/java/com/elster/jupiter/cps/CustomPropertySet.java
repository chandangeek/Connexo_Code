package com.elster.jupiter.cps;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

/**
 * Models the behavior of a component that will define
 * a custom set of properties that will extend the
 * standard properties of a domain class that is provided
 * by Connexo.
 * <p>
 * A CustomPropertySet is in essence a named wrapper for
 * a set of PropertySpecs. The implementator may consider
 * translating the name of the set using a Thesaurus.
 * In addition to providing the definition of the properties,
 * a CustomPropertySet will also be responsible for validating
 * business constraints that it may want to apply
 * to the values of the properties. A typical constraint that
 * is expected to be applied is dependencies between values.
 * As an example, when property ABC has value 1 then property
 * DEF can only have values 5, 10 and 43.
 * A CustomPropertySet can also apply uniqueness constraints.
 * A CustomPropertySet can be "versioned" at which point the history
 * of the values of every attribute are maintained at the object level.
 * </p>
 * <p>
 * The unique identifier that a CustomPropertySet is expected to provide
 * should be unique across all sets that will exist at runtime
 * We think that the number of customizations per installation will be
 * limited such that managing the uniqueness will not be a problem,
 * even when the service and delivery team defines reusable attribute sets
 * that they use across customers.
 * The unique identifier is used by the storage mechanism in a way that
 * constraints the length of the identifier to a maximum of 28 characters.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (12:59)
 */
@ConsumerType
public interface CustomPropertySet {

    /**
     * Uniquely identifies this set across all sets that are available in the system at runtime.
     *
     * @return This set's unique identifier
     */
    String getId();

    /**
     * Returns a String that is appropriate for displaying
     * this CustomPropertySet in a UI.
     * The implementation should consider using a Thesaurus
     * to make sure that the name is appropriate in all
     * languages to all users.
     *
     * @return The name for this CustomPropertySet
     */
    String getName();

    /**
     * Gets the domain for which this CustomPropertySet provides custom properties.
     *
     * @return The domain class that is extended by this CustomPropertySet
     */
    Class getDomainClass();

    /**
     * Returns <code>true</code> iff the properties
     * of this CustomPropertySet should be versioned.
     * When that is the case, the values of the properties
     * will be saved over time and an effective timestamp will
     * need to be provided when the properties are saved.
     *
     * @return A flag that indicates if the properties of this CustomPropertySet are versioned
     */
    boolean isVersioned();

    /**
     * Gets the List of {@link PropertySpec}s that defines all
     * of the properties for this CustomPropertySet.
     *
     * @return The List of PropertySpec
     */
    List<PropertySpec> getPropertySpecs();

    /**
     * Gets the List of {@link UniqueConstraintSpec} for this CustomPropertySet.
     *
     * @return The List of UniqueConstraintSpec
     */
    List<UniqueConstraintSpec> getUniquenessConstraints();

}