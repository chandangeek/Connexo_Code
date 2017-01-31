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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class DeviceTypeThreeDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {
        DOMAIN("device", "device"),
        TEST_ATTRIBUTE_NUMBER("testNumber", "test_number"),
        TEST_ATTRIBUTE_STRING("testString", "test_string");

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

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "CannotBeNull")
    private BigDecimal testNumber;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "FieldTooLong")
    private String testString;

    public DeviceTypeThreeDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public BigDecimal getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(BigDecimal testNumber) {
        this.testNumber = testNumber;
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        this.setTestNumber(new BigDecimal(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName()).toString()));
        this.setTestString((String) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_STRING.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), this.getTestNumber());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_STRING.javaName(), this.getTestString());
    }

    @Override
    public void validateDelete() {
    }
}