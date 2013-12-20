package com.energyict.mdc.protocol.device.messages;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.PropertySpec;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link DeviceMessageSpecPrimaryKey} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/02/13
 * Time: 15:33
 */
public class DeviceMessageSpecPrimaryKeyTest {

    @Test
    public void correctNumberTest(){
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = this.primaryKeyForNumericTestingPurposes();

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric("12")).isTrue();
    }

    @Test
    public void incorrectNumberTest(){
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = this.primaryKeyForNumericTestingPurposes();

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric("I'mNotAnNumber :)")).isFalse();
    }

    @Test
    public void invalidNumberTest(){
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = this.primaryKeyForNumericTestingPurposes();

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric(null)).isFalse();
    }

    @Test
    public void cleanUpClassNameTest() {
        // We actually don't care about the spec at all
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKey(
                DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS,
                DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.protocol.device.messages.DeviceMessageTestSpec");
    }

    @Test
    public void extendedCleanUpClassNameTest() {
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = this.primaryKeyForExtendedClassNameCleanupTestingPurposes();

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(ExtendedDeviceMessageTestWithoutOverriddenMethodSpec.TEST_SPEC_WITHOUT_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKeyTest$ExtendedDeviceMessageTestWithoutOverriddenMethodSpec");
    }

    @Test
    public void extendedCleanUpClassNameForSimpleEnumInnerClass(){
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = this.primaryKeyForExtendedClassNameCleanupTestingPurposes();

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(ExtendedDeviceMessageTestWithOverriddenMethodSpec.TEST_SPEC_WITH_SIMPLE_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKeyTest$ExtendedDeviceMessageTestWithOverriddenMethodSpec");
    }

    private DeviceMessageSpecPrimaryKey primaryKeyForNumericTestingPurposes () {
        // We actually don't care about the spec at all
        return new DeviceMessageSpecPrimaryKey(
                DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS,
                DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS.name());
    }

    private DeviceMessageSpecPrimaryKey primaryKeyForExtendedClassNameCleanupTestingPurposes () {
        // We actually don't care about the spec at all
        return new DeviceMessageSpecPrimaryKey(
                DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS,
                DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS.name());
    }

    public enum ExtendedDeviceMessageTestWithoutOverriddenMethodSpec implements DeviceMessageSpec {

        TEST_SPEC_WITH_SIMPLE_SPECS(
                new BigDecimalPropertySpec("testMessageSpec.simpleBigDecimal"),
                new StringPropertySpec("testMessageSpec.simpleString")),
        TEST_SPEC_WITHOUT_SPECS;

        private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.CONNECTIVITY_SETUP;

        private List<PropertySpec> deviceMessagePropertySpecs;

        ExtendedDeviceMessageTestWithoutOverriddenMethodSpec(PropertySpec... deviceMessagePropertySpecs) {
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

    public enum ExtendedDeviceMessageTestWithOverriddenMethodSpec implements DeviceMessageSpec {

        TEST_SPEC_WITH_SIMPLE_SPECS(
                new BigDecimalPropertySpec("testMessageSpec.simpleBigDecimal"),
                new StringPropertySpec("testMessageSpec.simpleString")) {
        },
        TEST_SPEC_WITHOUT_SPECS {
        };

        private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.CONNECTIVITY_SETUP;

        private List<PropertySpec> deviceMessagePropertySpecs;

        ExtendedDeviceMessageTestWithOverriddenMethodSpec(PropertySpec... deviceMessagePropertySpecs) {
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

}