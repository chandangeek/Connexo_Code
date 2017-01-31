/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.ChannelSpec;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class LoadProfileTwoVersionedDomainExtension extends AbstractVersionedPersistentDomainExtension  implements PersistentDomainExtension<ChannelSpec> {

    public enum FieldNames {
        DOMAIN("channelSpec", "channelSpec"),
        DEVICE("device", "device"),
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

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "CannotBeNull")
    private BigDecimal device;
    @NotNull(groups = {Save.Create.class, Save.Update.class})
    private BigDecimal testEnumNumberA;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "FieldTooLong")
    private String testEnumStringB;
    private boolean testBooleanC;

    public LoadProfileTwoVersionedDomainExtension() {
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

    public Interval getInterval() {
        return super.getInterval();
    }

    public void setInterval(Interval interval) {
        super.setInterval(interval);
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
    public void copyFrom(ChannelSpec channelSpec, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.channelSpec.set(channelSpec);
        this.setDevice(new BigDecimal(additionalPrimaryKeyValues[0].toString()));
        this.setTestEnumNumber(new BigDecimal(propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName()).toString()));
        this.setTestEnumString((String) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName()));
        this.setTestBoolean((boolean) propertyValues.getProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), this.getTestEnumNumber());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), this.getTestEnumString());
        propertySetValues.setProperty(FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), this.getTestBoolean());
    }

    @Override
    public void validateDelete() {
    }
}