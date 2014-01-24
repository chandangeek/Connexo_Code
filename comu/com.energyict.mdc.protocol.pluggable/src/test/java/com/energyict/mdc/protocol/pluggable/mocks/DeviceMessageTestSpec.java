package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

/**
 * Test enum implementing DeviceMessageSpec
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/02/13
 * Time: 15:16
 */
public enum DeviceMessageTestSpec implements DeviceMessageSpec {

    TEST_SPEC_WITH_SIMPLE_SPECS(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(Constants.SIMPLE_BIGDECIMAL_PROPERTY_SPEC_NAME),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(Constants.SIMPLE_STRING_PROPERTY_SPEC_NAME)),
    TEST_SPEC_WITH_EXTENDED_SPECS(
            RequiredPropertySpecFactory.newInstance().referencePropertySpec(Constants.CODETABLE_PROPERTY_SPEC_NAME, getCodeFactory()),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(Constants.ACTIVATIONDATE_PROPERTY_SPEC_NAME)),
    TEST_SPEC_WITHOUT_SPECS;

    public static class Constants {
        public static final String CODETABLE_PROPERTY_SPEC_NAME = "testMessageSpec.codetable";
        public static final String ACTIVATIONDATE_PROPERTY_SPEC_NAME = "testMessageSpec.activationdate";
        public static final String SIMPLE_STRING_PROPERTY_SPEC_NAME = "testMessageSpec.simpleString";
        public static final String SIMPLE_BIGDECIMAL_PROPERTY_SPEC_NAME = "testMessageSpec.simpleBigDecimal";
    }

    private static IdBusinessObjectFactory getCodeFactory() {
        return (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.CODE.id());
    }

    private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.FIRST_TEST_CATEGORY;

    private List<PropertySpec> deviceMessagePropertySpecs;

    DeviceMessageTestSpec(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return activityCalendarCategory;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

}