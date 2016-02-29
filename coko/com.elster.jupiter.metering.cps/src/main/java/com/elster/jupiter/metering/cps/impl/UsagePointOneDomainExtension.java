package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class UsagePointOneDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum FieldNames {
        DOMAIN("usagePoint", "usage_point"),
        TEST_ATTRIBUTE_NUMBER("testNumber", "test_number"),
        TEST_ATTRIBUTE_STRING("testString", "test_string"),
        TEST_ATTRIBUTE_ENUM_NUMBER("testEnumNumber", "test_enum_number"),
        TEST_ATTRIBUTE_ENUM_STRING("testEnumString", "test_enum_string"),
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

    private Reference<UsagePoint> usagePoint = Reference.empty();
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();


    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{NotNumber}")
    @Min(value = 0, groups = {Save.Create.class, Save.Update.class}, message = "{NumberMinValue}")
    @Max(value = 999999999, groups = {Save.Create.class, Save.Update.class}, message = "{NumberMaxValue}")
    private BigDecimal testNumber;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{CannotBeNull}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{FieldTooLong}")
    private String testString;
    @Min(value = 0, groups = {Save.Create.class, Save.Update.class}, message = "{NumberMinValue}")
    @Max(value = 999999999, groups = {Save.Create.class, Save.Update.class}, message = "{NumberMaxValue}")
    private BigDecimal testEnumNumber;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{FieldTooLong}")
    private String testEnumString;
    private boolean testBoolean;

    public UsagePointOneDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public boolean getTestBoolean() {
        return testBoolean;
    }

    public void setTestBoolean(Boolean testBoolean) {
        this.testBoolean = testBoolean;
    }

    public String getTestEnumString() {
        return testEnumString;
    }

    public void setTestEnumString(String testEnumString) {
        this.testEnumString = testEnumString;
    }

    public BigDecimal getTestEnumNumber() {
        return testEnumNumber;
    }

    public void setTestEnumNumber(BigDecimal testEnumNumber) {
        this.testEnumNumber = testEnumNumber;
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
    public void copyFrom(UsagePoint usagePoint, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(usagePoint);
        this.setTestNumber(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName()) != null ?
                new BigDecimal(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName()).toString()) : null);
        this.setTestString((String) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_STRING.javaName()));
        this.setTestEnumNumber(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName()) != null ?
                new BigDecimal(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName()).toString()) : null);
        this.setTestEnumString((String) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName()));
        this.setTestBoolean((boolean) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), this.getTestNumber());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_STRING.javaName(), this.getTestString());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), this.getTestEnumNumber());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), this.getTestEnumString());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), this.getTestBoolean());
    }

    @Override
    public void validateDelete() {
    }
}