/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.RegisterSpec;

import java.math.BigDecimal;
import java.util.Optional;

public class DeviceRegisterSAPInfoDomainExtension extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<RegisterSpec>, Effectivity {

    public enum FieldNames {
        DOMAIN("registerSpec", "REGISTERSPEC"),
        DEVICE_ID("device", "DEVICE"),
        LOGICAL_REGISTER_NUMBER("logicalRegisterNumber", "LOGICAL_REGISTER_NUMBER");

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

    private Reference<RegisterSpec> registerSpec = ValueReference.absent();
    private Long device;
    private BigDecimal logicalRegisterNumber;

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(RegisterSpec registerSpec, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.registerSpec.set(registerSpec);
        this.device = additionalPrimaryKeyValues.length > 0 ? (Long) additionalPrimaryKeyValues[0] : null;
        this.logicalRegisterNumber = (BigDecimal) propertyValues.getProperty(FieldNames.LOGICAL_REGISTER_NUMBER.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        additionalPrimaryKeyValues[0] = this.device;
        propertySetValues.setProperty(FieldNames.LOGICAL_REGISTER_NUMBER.javaName(), this.logicalRegisterNumber);
    }

    @Override
    public void validateDelete() {
        // for future purposes
    }

    public long getDeviceId() {
        return device;
    }

    public void setDeviceId(long id) {
        device = id;
    }

    @Override
    public Interval getInterval() {
        return super.getInterval();
    }

    @Override
    public void setInterval(Interval interval) {
        super.setInterval(interval);
    }

    public RegisterSpec getRegisterSpec() {
        return registerSpec.get();
    }

    public Optional<BigDecimal> getLogicalRegisterNumber() {
        return Optional.ofNullable(logicalRegisterNumber);
    }
}
