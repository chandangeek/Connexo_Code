/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.data.Device;

import java.math.BigDecimal;
import java.util.Optional;

public class DeviceSAPInfoDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {
        DOMAIN("device", "DEVICE"),
        DEVICE_IDENTIFIER("deviceIdentifier", "DEVICE_IDENTIFIER");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<Device> device = Reference.empty();
    private BigDecimal deviceIdentifier;

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        setDeviceIdentifier((BigDecimal) propertyValues.getProperty(FieldNames.DEVICE_IDENTIFIER.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_IDENTIFIER.javaName(), deviceIdentifier);
    }

    @Override
    public void validateDelete() {
        // for future purposes
    }

    public Optional<BigDecimal> getDeviceIdentifier() {
        return Optional.ofNullable(deviceIdentifier);
    }

    public void setDeviceIdentifier(BigDecimal deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public Device getDevice() {
        return device.get();
    }
}
