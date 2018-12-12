/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.Reference;

import javax.validation.constraints.NotNull;

/**
 * Defines the minimal behavior for any component that will
 * be providing device dialect properties as a {@link PersistentDomainExtension}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (11:38)
 */
public abstract class CommonDeviceProtocolDialectProperties extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<DeviceProtocolDialectPropertyProvider> {

    public enum Fields {
        DIALECT_PROPERTY_PROVIDER {
            @Override
            public String javaName() {
                return "dialectPropertyProvider";
            }

            @Override
            public String databaseName() {
                return "DIALECT_PROPS_PROVIDER";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

    }
    @NotNull
    private Reference<DeviceProtocolDialectPropertyProvider> dialectPropertyProvider = Reference.empty();

    @Override
    public void copyFrom(DeviceProtocolDialectPropertyProvider dialectPropertyProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.dialectPropertyProvider.set(dialectPropertyProvider);
        this.copyActualPropertiesFrom(propertyValues);
    }

    protected abstract void copyActualPropertiesFrom(CustomPropertySetValues propertyValues);

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        /* We could also not implement this method and have subclasses implement it
         * but that would create an incosisten api. Now subclasses have two methods to implement:
         * copyActualPropertiesFrom
         * copyActualPropertiesTo
         */
        this.copyActualPropertiesTo(propertySetValues);
    }

    protected abstract void copyActualPropertiesTo(CustomPropertySetValues propertySetValues);

    /**
     * Copies the specified values to the CustomPropertySetValues if it is not null.
     *
     * @param propertySetValues The CustomPropertySetValues
     * @param propertyName The name of the property
     * @param propertyValue The value
     */
    protected void setPropertyIfNotNull(CustomPropertySetValues propertySetValues, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            propertySetValues.setProperty(propertyName, propertyValue);
        }
    }

}