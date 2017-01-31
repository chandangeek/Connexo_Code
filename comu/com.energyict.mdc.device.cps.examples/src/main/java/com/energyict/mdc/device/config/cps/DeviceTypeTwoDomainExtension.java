/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.data.Device;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class DeviceTypeTwoDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {
        DOMAIN("device", "device"),
        TEST_ATTRIBUTE_NUMBER("testNumberTwo", "test_number"),
        TEST_ATTRIBUTE_ENUM_NUMBER("testEnumNumberTwo", "test_enum_number"),
        TEST_ATTRIBUTE_BOOLEAN("testBoolean", "test_boolean");

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
    private BigDecimal testNumberTwo;
    @NotNull(groups = {Save.Create.class, Save.Update.class})
    private BigDecimal testEnumNumberTwo;
    private boolean testBoolean;

    public DeviceTypeTwoDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public boolean getTestBoolean() {
        return testBoolean;
    }

    public void setTestBoolean(Boolean testBoolean) {
        this.testBoolean = testBoolean;
    }

    public BigDecimal getTestEnumNumber() {
        return testEnumNumberTwo;
    }

    public void setTestEnumNumber(BigDecimal testEnumNumber) {
        this.testEnumNumberTwo = testEnumNumber;
    }

    public BigDecimal getTestNumber() {
        return testNumberTwo;
    }

    public void setTestNumber(BigDecimal testNumber) {
        this.testNumberTwo = testNumber;
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        this.setTestNumber(new BigDecimal(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName()).toString()));
        this.setTestEnumNumber(new BigDecimal(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName()).toString()));
        this.setTestBoolean((boolean) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), this.getTestNumber());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), this.getTestEnumNumber());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), this.getTestBoolean());
    }

    @Override
    public void validateDelete() {
    }
}