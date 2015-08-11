package com.elster.jupiter.cps;

import aQute.bnd.annotation.ConsumerType;

/**
 * <p>
 * Models the behavior of the persistence class of a {@link CustomPropertySet}.
 * Note that the implementation class must have a field defined as:
 * <pre>
 *     <code>
 *         private Reference<CustomPropertySet> customPropertySet = Reference.absent();
 *     </code>
 * </pre>
 * </p>
 * @param <T> The domain class that this extension is extending
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-11 (11:20)
 */
@ConsumerType
public interface PersistentDomainExtension<T> {

    /**
     * Initializes this extension with the domain object
     * that is being extended and the {@link CustomPropertySetValues}
     * for the {@link CustomPropertySet} that describes
     * the extension.
     *
     * @param domainInstance The domain object that is being extended
     * @param customPropertySet The CustomPropertySet
     * @param propertyValues The CustomPropertySetValues
     */
    void initializeFrom(T domainInstance, CustomPropertySet customPropertySet, CustomPropertySetValues propertyValues);

}