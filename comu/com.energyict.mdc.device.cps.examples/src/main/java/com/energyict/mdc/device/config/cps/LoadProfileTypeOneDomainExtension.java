package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.config.ChannelSpec;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class LoadProfileTypeOneDomainExtension implements PersistentDomainExtension<ChannelSpec> {

    public enum FieldNames {
        DOMAIN("channelSpec", "channelSpec"),
        TEST_ATTRIBUTE_ENUM_NUMBER("testEnumNumberA", "test_enum_number"),
        TEST_ATTRIBUTE_ENUM_STRING("testEnumStringB", "test_enum_string"),
        TEST_ATTRIBUTE_BOOLEAN("testBooleanC", "test_boolean");

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

    private Reference<ChannelSpec> channelSpec = Reference.empty();
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    @NotNull(groups = {Save.Create.class, Save.Update.class})
    private BigDecimal testEnumNumberA;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "FieldTooLong")
    private String testEnumStringB;
    private boolean testBooleanC;

    public LoadProfileTypeOneDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public boolean getTestBoolean() {
        return testBooleanC;
    }

    public void setTestBoolean(Boolean testBoolean) {
        this.testBooleanC = testBoolean;
    }

    public String getTestEnumString() {
        return testEnumStringB;
    }

    public void setTestEnumString(String testEnumString) {
        this.testEnumStringB = testEnumString;
    }

    public BigDecimal getTestEnumNumber() {
        return testEnumNumberA;
    }

    public void setTestEnumNumber(BigDecimal testEnumNumber) {
        this.testEnumNumberA = testEnumNumber;
    }

    @Override
    public void copyFrom(ChannelSpec channelSpec, CustomPropertySetValues propertyValues) {
        this.channelSpec.set(channelSpec);
        this.setTestEnumNumber(new BigDecimal(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName()).toString()));
        this.setTestEnumString((String) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName()));
        this.setTestBoolean((boolean) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), this.getTestEnumNumber());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), this.getTestEnumString());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), this.getTestBoolean());
    }
}