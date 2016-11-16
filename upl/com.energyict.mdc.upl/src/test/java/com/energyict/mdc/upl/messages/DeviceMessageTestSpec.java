package com.energyict.mdc.upl.messages;

import com.energyict.mdc.upl.properties.PropertySpec;

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

    TEST_SPEC_WITH_SIMPLE_SPECS(PropertySpecFactory.bigDecimalPropertySpec("testMessageSpec.simpleBigDecimal"),
            PropertySpecFactory.stringPropertySpec("testMessageSpec.simpleString")),
    TEST_SPEC_WITH_EXTENDED_SPECS(PropertySpecFactory.codeTableReferencePropertySpec("testMessageSpec.codetable"),
            PropertySpecFactory.dateTimePropertySpec("testMessageSpec.activationdate")),
    TEST_SPEC_WITHOUT_SPECS;

    private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.CONNECTIVITY_SETUP;

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
    public long getMessageId() {
        return 0;
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
