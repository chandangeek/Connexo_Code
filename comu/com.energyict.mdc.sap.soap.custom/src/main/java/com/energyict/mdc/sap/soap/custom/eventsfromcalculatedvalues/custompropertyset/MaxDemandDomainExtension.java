/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MaxDemandDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {
        DOMAIN("device", "DEVICE"),
        CONNECTED_LOAD("connectedLoad", "CONNECTED_LOAD"),
        UNIT("unit", "UNIT"),
        CHECK_ENABLED("checkEnabled", "CHECK_ENABLED");

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
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal connectedLoad;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Unit unit;
    private boolean checkEnabled;

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        setConnectedLoad((BigDecimal) propertyValues.getProperty(FieldNames.CONNECTED_LOAD.javaName()));
        setUnit((Unit) propertyValues.getProperty(FieldNames.UNIT.javaName()));
        setCheckEnabled((boolean) propertyValues.getProperty(FieldNames.CHECK_ENABLED.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CONNECTED_LOAD.javaName(), connectedLoad);
        propertySetValues.setProperty(FieldNames.UNIT.javaName(), unit);
        propertySetValues.setProperty(FieldNames.CHECK_ENABLED.javaName(), checkEnabled);
    }

    public void setConnectedLoad(BigDecimal connectedLoad) {
        this.connectedLoad = connectedLoad;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setCheckEnabled(Boolean checkEnabled) {
        this.checkEnabled = checkEnabled;
    }

    public Device getDevice() {
        return device.get();
    }

    public BigDecimal getConnectedLoad() {
        return connectedLoad;
    }

    public Unit getUnit() {
        return unit;
    }

    public boolean isCheckEnabled() {
        return checkEnabled;
    }

    @Override
    public void validateDelete() {
    }
}
