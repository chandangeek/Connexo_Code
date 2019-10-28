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
        FLAG("flag", "FLAG");

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
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private boolean flag;

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        setCTRatio(new BigDecimal(propertyValues.getProperty(FieldNames.CT_RATIO.javaName()).toString()));
        setFlag((boolean) propertyValues.getProperty(FieldNames.FLAG.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CT_RATIO.javaName(), ctRatio);
        propertySetValues.setProperty(FieldNames.FLAG.javaName(), flag);
    }

    public void setCTRatio(BigDecimal ctRatio) {
        this.ctRatio = ctRatio;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public BigDecimal getCtRatio() {
        return ctRatio;
    }

    public boolean isFlag() {
        return flag;
    }

    public Device getDevice() {
        return device.get();
    }

    @Override
    public void validateDelete() {
    }
}
