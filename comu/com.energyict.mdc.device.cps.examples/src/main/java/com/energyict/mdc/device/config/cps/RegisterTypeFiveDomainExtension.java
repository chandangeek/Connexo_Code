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
import com.energyict.mdc.common.device.config.RegisterSpec;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class RegisterTypeFiveDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<RegisterSpec> {

    public enum FieldNames {
        DOMAIN("registerSpec", "registerSpec"),
        DEVICE("device", "device"),
        TEST_ATTRIBUTE_NUMBER("testNumber", "test_number"),
        TEST_ATTRIBUTE_STRING("testString", "test_string"),
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

    private Reference<RegisterSpec> registerSpec = Reference.empty();

    private BigDecimal device;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "CannotBeNull")
    private BigDecimal testNumber;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "FieldTooLong")
    private String testString;
    private boolean testBoolean;

    public RegisterTypeFiveDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public BigDecimal getDevice() {
        return device;
    }

    public void setDevice(BigDecimal device) {
        this.device = device;
    }

    public boolean getTestBoolean() {
        return testBoolean;
    }

    public void setTestBoolean(Boolean testBoolean) {
        this.testBoolean = testBoolean;
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
    public void copyFrom(RegisterSpec registerSpec, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.registerSpec.set(registerSpec);
        this.setDevice(additionalPrimaryKeyValues.length > 0 ? new BigDecimal(additionalPrimaryKeyValues[0].toString()) : null);
        this.setTestNumber(new BigDecimal(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName()).toString()));
        this.setTestString((String) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_STRING.javaName()));
        this.setTestBoolean((boolean) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), this.getTestNumber());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_STRING.javaName(), this.getTestString());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), this.getTestBoolean());
    }

    @Override
    public void validateDelete() {
    }
}