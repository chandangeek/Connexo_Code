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

    String COMPONENT_NAME = "CPS";

    /**
     * Registers the specified {@link CustomPropertySet} on this service's whiteboard.
     * This will enable the custom properties on the set's domain class,
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
     * @see #addCustomPropertySet(CustomPropertySet)
     */
    void removeCustomPropertySet(CustomPropertySet customPropertySet);

    /**
     * Registers the specified {@link CustomPropertySet}
     * that is defined by the system on this service's whiteboard.
     * Note that system defined CustomPropertySets cannot be edited
     * by the administrator so the component that registers the component
     * will also have to set the view and edit privileges.
     * The latter also implies that you are responsible for the transaction management.
     *
     * @param customPropertySet The CustomPropertySet
     */
    void addSystemCustomPropertySet(CustomPropertySet customPropertySet);

    /**
     * Removes the specified {@link CustomPropertySet} that was
     * previously registered by the system from this service's whiteboard.
     *
     * @param customPropertySet The CustomPropertySet
     * @see #addSystemCustomPropertySet(CustomPropertySet)
     */
    void removeSystemCustomPropertySet(CustomPropertySet customPropertySet);

    /**
     * Finds all {@link RegisteredCustomPropertySet}s that are currently active.
     * For remembrence's sake:
     * <ul>
     * <li>a CustomPropertySet that has registered before but is currently not deployed in the OSGi container will NOT be returned</li>
     * <li>a CustomPropertySet that was registered by the system will NOT be returned</li>
     * </ul>
     *
     * @return The List of RegisteredCustomPropertySet
     */
    List<RegisteredCustomPropertySet> findActiveCustomPropertySets();

    /**
     * Finds all {@link RegisteredCustomPropertySet}s that are currently active
     * and that provide custom properties for the specified domain class.
     * For remembrence's sake:
     * <ul>
     * <li>a CustomPropertySet that has registered before but is currently not deployed in the OSGi container will NOT be returned</li>
     * <li>a CustomPropertySet that was registered by the system will NOT be returned</li>
     * </ul>
     *
     * @param domainClass The domain class
     * @return The List of RegisteredCustomPropertySet
     */
    List<RegisteredCustomPropertySet> findActiveCustomPropertySets(Class domainClass);

    /**
     * Finds the {@link RegisteredCustomPropertySet}s for the {@link CustomPropertySet}
     * that registered with the service's whiteboard before.
     * Note that this will consider both system and user defined CustomPropertySets.
     *
     * @param id The unique identifier of the CustomPropertySet
     * @return The List of RegisteredCustomPropertySet
     */
    Optional<RegisteredCustomPropertySet> findActiveCustomPropertySet(String id);

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
     * @param additionalPrimaryKeyValues Values for the addition primary keys defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object additionalPrimayKeyValues);

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
     * the specified businesObject object at the specified point in time.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned because in that case
     * you do not need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param effectiveTimestamp The point in time
     * @param additionalPrimaryKeyValues Values for the addition primary keys defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp, Object additionalPrimayKeyValues);

    /**
     * Sets the values for the {@link CustomPropertySet} against
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
     * @param effectiveTimestamp The point in time from which the new values are effective onwards
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
     * @param additionalPrimaryKeyValues Values for the addition primary keys defined by the CustomPropertySet
     * @param <D> The domain class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> Optional<T> getValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues);

    /**
     * Gets the values for the {@link CustomPropertySet} that were saved for
     * the specified businesObject object at the specified point in time.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned because in that case
     * you do not need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param effectiveTimestamp The point in time
     * @param additionalPrimaryKeyValues Values for the addition primary keys defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> Optional<T> getValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues);

    /**
     * Removes all the values for the {@link CustomPropertySet} that
     * were saved for the specified businesObject object before.
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @see #setValuesFor(CustomPropertySet, Object, CustomPropertySetValues)
     * @see #setValuesFor(CustomPropertySet, Object, CustomPropertySetValues, Instant)
     */
    <D, T extends PersistentDomainExtension<D>> void removeValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject);

}