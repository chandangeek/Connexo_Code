/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.MessageSeeds;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PowerFactorDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {
        DOMAIN("device", "DEVICE"),
        SETPOINT_THRESHOLD("setpointThreshold", "SETPOINT_THRESHOLD"),
        HYSTERESIS_PERCENTAGE("hysteresisPercentage", "HYSTERESIS_PERCENTAGE"),
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
    private BigDecimal setpointThreshold;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @DecimalMin(message = "{" + MessageSeeds.Keys.VALUE_MUST_BE_POSITIVE + "}", value = "0", groups = {Save.Create.class, Save.Update.class})
    @DecimalMax(message = "{" + MessageSeeds.Keys.PERCENTAGE_VALUE_NOT_VALID + "}", value = "100", groups = {Save.Create.class, Save.Update.class})
    private BigDecimal hysteresisPercentage;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private boolean flag;

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        setSetpointThreshold(new BigDecimal(propertyValues.getProperty(FieldNames.SETPOINT_THRESHOLD.javaName()).toString()));
        setHysteresisPercentage(new BigDecimal(propertyValues.getProperty(FieldNames.HYSTERESIS_PERCENTAGE.javaName()).toString()));
        setFlag((boolean) propertyValues.getProperty(FieldNames.FLAG.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.SETPOINT_THRESHOLD.javaName(), setpointThreshold);
        propertySetValues.setProperty(FieldNames.HYSTERESIS_PERCENTAGE.javaName(), hysteresisPercentage);
        propertySetValues.setProperty(FieldNames.FLAG.javaName(), flag);
    }

    public void setSetpointThreshold(BigDecimal setpointThreshold) {
        this.setpointThreshold = setpointThreshold;
    }

    public void setHysteresisPercentage(BigDecimal hysteresisPercentage) {
        this.hysteresisPercentage = hysteresisPercentage;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public Device getDevice() {
        return device.get();
    }

    @Override
    public void validateDelete() {
    }
}
