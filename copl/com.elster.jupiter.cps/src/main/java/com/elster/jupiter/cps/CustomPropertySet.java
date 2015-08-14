package com.elster.jupiter.cps;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.time.Interval;

import java.util.List;
import java.util.Set;

/**
 * Models the behavior of a component that will define
 * a custom set of properties that will extend the
 * standard properties of a domain class that is provided
 * by Connexo.
 * <p>
 * A CustomPropertySet is in essence a named wrapper for
 * a set of PropertySpecs. The implementator should consider
 * translating the name of the set using a Thesaurus.
 * In addition to providing the definition of the properties,
 * a CustomPropertySet will also provide a persistence class
 * that will be involved in validating business constraints.
 * A CustomPropertySet can also apply uniqueness constraints.
 * A CustomPropertySet can be "versioned" at which point the history
 * of the values of every attribute are maintained at the object level.
 * Note that this also imposes requirements on the persistence class.
 * It should implement the {@link Effectivity} interface and have
 * a field of type {@link Interval} by the name of "interval".
 * </p>
 * <p>
 * The unique identifier that a CustomPropertySet is expected to provide
 * should be unique across all sets that will exist at runtime
 * We think that the number of customizations per installation will be
 * limited such that managing the uniqueness will not be a problem,
 * even when the service and delivery team defines reusable attribute sets
 * that they use across customers.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (12:59)
 * @param <D> The domain class that is being extended by this CustomPropertySet
 * @param <T> The class that holds persistent values for this CustomPropertySet
 */
@ConsumerType
public interface CustomPropertySet<D, T extends PersistentDomainExtension<D>> {

    /**
     * Uniquely identifies this set across all sets that are available in the system at runtime.
     *
     * @return This set's unique identifier
     */
    default String getId() {
        return getPersistenceSupport().persistenceClass().getName();
    }

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
    Class<D> getDomainClass();

    PersistenceSupport<D, T> getPersistenceSupport();

    /**
     * Tests if every domain object that is extended by this CustomPropertySet
     * is required to have values.
     * If this is not the case, it will be possible to create domain objects
     * without values for even the required properties of this CustomPropertySet.
     * Note that the {@link CustomPropertySetService} is not capable of validating
     * this because the service that manages the domain object that is being
     * extended, is responsible for passing the values to the CustomPropertySetService
     * when the managed object is created or updated.
     *
     * @return A flag that indicates if this CustomPropertySet is required
     */
    boolean isRequired();

    /**
     * Returns <code>true</code> iff the properties
     * of this CustomPropertySet should be versioned.
     * When that is the case, the values of the properties
     * will be saved over time and an effective timestamp will
     * need to be provided when the properties are saved.
     * In addition, the persistence class should implement
     * the {@link Effectivity} interface and have a field
     * of type {@link Interval} by the name of "interval".
     *
     * @return A flag that indicates if the properties of this CustomPropertySet are versioned
     */
    boolean isVersioned();

    /**
     * The default set of {@link ViewPrivilege}s.
     *
     * @return The default view privileges
     */
    public Set<ViewPrivilege> defaultViewPrivileges();

    /**
     * The default set of {@link EditPrivilege}s.
     *
     * @return The default edit privileges
     */
    public Set<EditPrivilege> defaultEditPrivileges();

    /**
     * Gets the List of {@link PropertySpec}s that defines all
     * of the properties for this CustomPropertySet.
     *
     * @return The List of PropertySpec
     */
    List<PropertySpec> getPropertySpecs();

}