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

public class CTRatioDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {
        DOMAIN("device", "DEVICE"),
        CT_RATIO("ctRatio", "CT_RATIO"),
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
    private BigDecimal ctRatio;
    private boolean checkEnabled;

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        setCTRatio((BigDecimal) propertyValues.getProperty(FieldNames.CT_RATIO.javaName()));
        setCheckEnabled((boolean) propertyValues.getProperty(FieldNames.CHECK_ENABLED.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CT_RATIO.javaName(), ctRatio);
        propertySetValues.setProperty(FieldNames.CHECK_ENABLED.javaName(), checkEnabled);
    }

    public void setCTRatio(BigDecimal ctRatio) {
        this.ctRatio = ctRatio;
    }

    public void setCheckEnabled(Boolean checkEnabled) {
        this.checkEnabled = checkEnabled;
    }

    public BigDecimal getCtRatio() {
        return ctRatio;
    }

    public boolean isCheckEnabled() {
        return checkEnabled;
    }

    public Device getDevice() {
        return device.get();
    }

    @Override
    public void validateDelete() {
    }
}
