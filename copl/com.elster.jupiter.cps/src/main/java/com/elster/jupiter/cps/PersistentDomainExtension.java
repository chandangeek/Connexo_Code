package com.elster.jupiter.cps;

import aQute.bnd.annotation.ConsumerType;

/**
 * <p>
 * Models the behavior of the persistence class of a {@link CustomPropertySet}.
 * Note that the implementation class must have the fields described in {@link HardCodedFieldNames}.
 * </p>
 * Note also that the connexo dependency injection mechanism is available to be used
 * by the implementation class. The following objects/services will be injected if needed:
 * <ul>
 * <li>{@link com.elster.jupiter.orm.DataModel}: The DataModel that holds the table of your domain extension should you need to run queries</li>
 * <li>{@link CustomPropertySetService}: The CustomPropertySetService</li>
 * </ul>
 * If the implementation class need any additional services,
 * be it connexo services or services that the custom code is providing
 * then the {@link PersistenceSupport} implementation class should
 * return a Module that will bind these services.
 *
 * @param <T> The domain class that this extension is extending
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-11 (11:20)
 */
@ConsumerType
public interface PersistentDomainExtension<T> {

    /**
     * Copies the extended properties contained in the {@link CustomPropertySetValues}
     * into this PersistentDomainExtension.
     * If the related CustomPropertySet is versioned,
     * the Interval will be copied for you.
     *
     * @param domainInstance The domain object that is being extended
     * @param customPropertySet The CustomPropertySet
     * @param propertyValues The CustomPropertySetValues
     */
    void copyFrom(T domainInstance, CustomPropertySet customPropertySet, CustomPropertySetValues propertyValues);

    /**
     * Copies the extension properties into the {@link CustomPropertySetValues}.
     * If the related CustomPropertySet is versioned,
     * the Interval will be copied into the CustomPropertySetValues for you.
     *
     * @param propertySetValues The CustomPropertySetValues
     */
    void copyTo(CustomPropertySetValues propertySetValues);

}