/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides support for adding custom properties to
 * entities that are provided by all Connexo bundles.
 * <p>
 * The API has two flavours. The first one will focus on generic client code,
 * i.e. code that is not really aware of the actual properties that are defined
 * by a {@link CustomPropertySet} and the related semantics of these properties.
 * The Connexo UI is an example of such client code.
 * This API is defined in terms of {@link CustomPropertySetValues}.
 * A second API focusses on client code that is aware of the properties
 * of a CustomPropertySet and the related semantics and will therefore
 * be interested to execute business related code on top of these custom properties.
 * This second API is defined in terms of the peristent entities that are
 * associated with the CustomPropertySet as is determinded by the
 * class returned by {@link PersistenceSupport#persistenceClass()}.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (15:37)
 */
@ProviderType
public interface CustomPropertySetService {

    String COMPONENT_NAME = "CPS";

    /**
     * Returns a SqlFragment that selects the values of the specified
     * {@link PropertySpec} that are stored against the specified business object.
     * The column that holds the values of the property is given the specified alias.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet <strong>IS</strong> versioned because in that case
     * you need to specify a Range of time when the values are effective.
     *
     * @param customPropertySet The CustomPropertySet
     * @param propertySpec The PropertySpec
     * @param alias The alias name for the column that holds the values of the property
     * @param businessObject The businesObject object
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The SqlFragment
     */
    <D, T extends PersistentDomainExtension<D>> SqlFragment getRawValuesSql(CustomPropertySet<D, T> customPropertySet, PropertySpec propertySpec, String alias, D businessObject, Object... additionalPrimaryKeyValues);

    /**
     * Returns a SqlFragment that selects the values of the specified
     * {@link PropertySpec} that are stored against the specified business object.
     * The column that holds the values of the property is given the specified alias.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned because in that case
     * you do not need to specify a Range of time when the values are effective.
     *
     * @param customPropertySet The CustomPropertySet
     * @param propertySpec The PropertySpec
     * @param alias The alias name for the column that holds the values of the property
     * @param businessObject The businesObject object
     * @param effectiveInterval The Range of time during which the value of the property should be effective
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The SqlFragment
     */
    <D, T extends PersistentDomainExtension<D>> SqlFragment getRawValuesSql(CustomPropertySet<D, T> customPropertySet, PropertySpec propertySpec, String alias, D businessObject, Range<Instant> effectiveInterval, Object... additionalPrimaryKeyValues);

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
     * Gets the unique set of values for the {@link CustomPropertySet}
     * that were saved for the specified businesObject object
     * and (optionally) the additional primary key columns.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is versioned because in that case
     * you need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getUniqueValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues);

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
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values, Object... additionalPrimaryKeyValues);

    /**
     * Gets the unique set of values for the {@link CustomPropertySet} that were saved for
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
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> CustomPropertySetValues getUniqueValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues);

    /**
     * Checks if the unique set of values for the {@link CustomPropertySet} that were saved for
     * the specified businesObject object at the specified point in time contains a value for
     * all of the given {@link PropertySpec}s.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned because in that case
     * you do not need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param effectiveTimestamp The point in time
     * @param specs The set of PropertySpecs that should be checked
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return true if a value was present for all of the given PropertySpecs, otherwise false
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>>  boolean hasValueForPropertySpecs(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp, Set<PropertySpec> specs, Object... additionalPrimaryKeyValues);

    /**
     * Gets all the versioned values for the {@link CustomPropertySet}
     * that were saved for the specified businesObject object.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> List<CustomPropertySetValues> getAllVersionedValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues);

    /**
     * Sets the values for the {@link CustomPropertySet} against
     * the specified businessObject object.
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
     * @param businessObject The businessObject object
     * @param effectiveTimestamp The point in time from which the new values are effective onwards
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param values The CustomPropertySetValues
     * @param <D> The businessObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businessObject, CustomPropertySetValues values, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues);

    /**
     * Sets the values for the {@link CustomPropertySet} against
     * the specified businessObject object.
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
     * @param businessObject The businessObject object
     * @param effectiveTimestamp The point in time from which the new values are effective onwards
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param persistentDomainExtension The persistent domain extension object containing the CustomPropertySetValues
     * @param <D> The businessObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businessObject, T persistentDomainExtension, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues);

    /**
     * Sets the values for the {@link CustomPropertySet} against
     * the specified businessObject object.
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
     * @param businessObject The businessObject object
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param persistentDomainExtension The persistent domain extension object containing the CustomPropertySetValues
     * @param <D> The businessObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> void setValuesFor(CustomPropertySet<D, T> customPropertySet, D businessObject, T persistentDomainExtension, Object... additionalPrimaryKeyValues);

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
     * @param additionalPrimaryKeyValues The values for the additional primary key columns as defined by the CustomPropertySet
     * @param <D> The domain class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The instance of the peristent class that holds the values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> Optional<T> getUniqueValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues);

    /**
     * Gets the values for the {@link CustomPropertySet} that match the Condition
     * specified with the {@link NonVersionedValuesEntityCustomConditionMatcher}.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is versioned because in that case
     * you need to use {@link #getVersionedValuesEntitiesFor(CustomPropertySet)}.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param <D> The domain class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The CustomPropertySetValues
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is versioned
     */
    <D, T extends PersistentDomainExtension<D>> NonVersionedValuesEntityCustomConditionMatcher<D, T> getNonVersionedValuesEntitiesFor(CustomPropertySet<D, T> customPropertySet);

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
     * @return The instance of the peristent class that holds the values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> Optional<T> getUniqueValuesEntityFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues);

    /**
     * Gets the values for the {@link CustomPropertySet} that match the condition
     * specified with the {@link VersionedValuesEntityCustomConditionMatcher}
     * and are affective at the timestamp specified with the {@link VersionedValuesEntityEffectivityMatcher}.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned because in that case
     * you do not need to specify an instant in time when the values are effective.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The VersionedValuesEntityCustomConditionMatcher
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> VersionedValuesEntityCustomConditionMatcher<D, T> getVersionedValuesEntitiesFor(CustomPropertySet<D, T> customPropertySet);

    /**
     * Gets all the values for the {@link CustomPropertySet} that were saved for
     * the specified businesObject object.
     * <p>
     * Note that this will throw an UnsupportedOperationException
     * when the CustomPropertySet is <strong>NOT</strong> versioned.
     * </p>
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param additionalPrimaryKeyValues Values for the addition primary keys defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @return The List of instances of the peristent class that holds the values for this CustomPropertySet
     * @see CustomPropertySet#isVersioned()
     * @throws UnsupportedOperationException Thrown when the CustomPropertySet is <strong>NOT</strong> versioned
     */
    <D, T extends PersistentDomainExtension<D>> List<T> getAllVersionedValuesEntitiesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues);

    <D, T extends PersistentDomainExtension<D>> void setValuesVersionFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values, Range<Instant> newRange, Object... addtionalPrimaryKeyValue);
    <D, T extends PersistentDomainExtension<D>> void setValuesVersionFor(CustomPropertySet<D, T> customPropertySet, D businesObject, CustomPropertySetValues values, Range<Instant> newRange, Instant effectiveTimestamp, Object... addtionalPrimaryKeyValue);

    <D, T extends PersistentDomainExtension<D>> OverlapCalculatorBuilder calculateOverlapsFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues);

    /**
     * Removes all the values for the {@link CustomPropertySet} that
     * were saved for the specified businesObject object before.
     *
     * @param customPropertySet The CustomPropertySet
     * @param businesObject The businesObject object
     * @param additionalPrimaryKeyValues Values for the addition primary keys defined by the CustomPropertySet
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     * @see #setValuesFor(CustomPropertySet, Object, CustomPropertySetValues, Object...)
     * @see #setValuesFor(CustomPropertySet, Object, CustomPropertySetValues, Instant, Object...)
     */
    <D, T extends PersistentDomainExtension<D>> void removeValuesFor(CustomPropertySet<D, T> customPropertySet, D businesObject, Object... additionalPrimaryKeyValues);

    /**
     * Supports specifying a search for values with a custom
     * Condition for a non-versioned CustomPropertySet.
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     */
    interface NonVersionedValuesEntityCustomConditionMatcher<D, T extends PersistentDomainExtension<D>> {
        List<T> matching(Condition condition);
    }

    /**
     * Supports specifying a search for values with a custom
     * Condition for a versioned CustomPropertySet.
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     */
    interface VersionedValuesEntityCustomConditionMatcher<D, T extends PersistentDomainExtension<D>> {
        VersionedValuesEntityEffectivityMatcher<D, T> matching(Condition condition);
    }

    /**
     * Finalizes the search for values of a versioned {@link CustomPropertySet}
     * by specifiying the point in time when the values should be effective.
     * @param <D> The businesObject class
     * @param <T> The class that holds persistent values for this CustomPropertySet
     */
    interface VersionedValuesEntityEffectivityMatcher<D, T extends PersistentDomainExtension<D>> {
        List<T> andEffectiveAt(Instant effectiveTimestamp);
    }

    <D, T extends PersistentDomainExtension<D>> void validateCustomPropertySetValues(CustomPropertySet<D, T> customPropertySet, CustomPropertySetValues values);
}