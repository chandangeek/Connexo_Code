/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.data.Device;

import javax.validation.constraints.Size;

public class DeviceTypeDemoCustomValidationDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {
        DOMAIN("device", "device"),
        METER_MECHANISM("meterMechanism", "meter_mechanism");

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

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "FieldTooLong")
    private String meterMechanism;

    public DeviceTypeDemoCustomValidationDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public String getMeterMechanism() {
        return meterMechanism;
    }

    public void setMeterMechanism(String meterMechanism) {
        this.meterMechanism = meterMechanism;
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        this.setMeterMechanism((String) propertyValues.getProperty(FieldNames.METER_MECHANISM.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.METER_MECHANISM.javaName(), this.getMeterMechanism());
    }

    @Override
    public void validateDelete() {
    }
}