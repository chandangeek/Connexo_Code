package com.elster.jupiter.cps;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides support for adding custom properties to
 * entities that are provided by all Connexo bundles.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (15:37)
 */
@ProviderType
public interface CustomPropertySetService {

    static String COMPONENT_NAME = "CPS";

    /**
     * Registers the specified {@link CustomPropertySet} on this service's whiteboard.
     * This will enables the custom properties on the set's domain class,
     * providing that the domain class is enabled for extension.
     *
     * @param customPropertySet The CustomPropertySet
     */
    void addCustomPropertySet(CustomPropertySet customPropertySet);

    /**
     * Removes the specified {@link CustomPropertySet} from this service's whiteboard.
     * This will disable the custom properties on the set's domain class.
     *
     * @param customPropertySet The CustomPropertySet
     */
    void removeCustomPropertySet(CustomPropertySet customPropertySet);

    /**
     * Finds all {@link RegisteredCustomPropertySet}s that are currently active.
     * For remembrence's sake: a CustomPropertySet may have registered before
     * but is currently not deployed in the OSGi container
     * and will therefore NOT be returned.
     *
     * @return The List of RegisteredCustomPropertySet
     */
    List<RegisteredCustomPropertySet> findActiveCustomPropertySets();

    /**
     * Finds all {@link RegisteredCustomPropertySet}s that are currently active
     * and that provide custom properties for the specified domain class.
     * For remembrence's sake: a CustomPropertySet may have registered before
     * but is currently not deployed in the OSGi container
     * and will therefore NOT be returned.
     *
     * @param domainClass The domain class
     * @return The List of RegisteredCustomPropertySet
     */
    List<RegisteredCustomPropertySet> findActiveCustomPropertySets(Class domainClass);

    /**
     * Gets the values for the {@link CustomPropertySet} that were saved for
     * the specified businesObject object.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is versioned because in that case
     * you need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject);

    /**
     * Sets the values for the {@link CustomPropertySet} that were saved for
     * the specified businesObject object.
     * <p>
     * Note that previous values are lost forever.If you do not want this,
     * then you need to mark the CustomPropertySet as versioned.
     * This is not something that you can change once the set is in use.
     * </p>
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is versioned because in that case
     * you need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param values The CustomPropertySetValues
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values);

    /**
     * Gets the values for the {@link CustomPropertySet} that were saved for
     * the specified businesObject object.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned because in that case
     * you do not need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp);

    /**
     * Sets the values for the {@link CustomPropertySet} that were saved for
     * the specified businesObject object.
     * <p>
     * Note that previous values are not lost but retained for historical purposes.
     * </p>
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned because in that case
     * you do not need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param values The CustomPropertySetValues
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values, Instant effectiveTimestamp);

    /**
     * Gets the values for the {@link CustomPropertySet} that were saved for
     * the specified domain object.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is versioned because in that case
     * you need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The domain object
     * @param <D> The domain class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> Optional<T> getValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject);

    /**
     * Gets the values for the {@link CustomPropertySet} that were saved for
     * the specified businesObject object.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned because in that case
     * you do not need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> Optional<T> getValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp);

}