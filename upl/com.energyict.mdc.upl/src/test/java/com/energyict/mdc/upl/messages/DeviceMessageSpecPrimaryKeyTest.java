package com.energyict.mdc.upl.messages;

import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKey(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS, DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric("12")).isTrue();
    }

    @Test
    public void incorrectNumberTest(){
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKey(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS, DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric("I'mNotAnNumber :)")).isFalse();
    }

    @Test
    public void invalidNumberTest(){
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKey(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS, DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric(null)).isFalse();
    }

    @Test
    public void cleanUpClassNameTest() {
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKey(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS, DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.messages.DeviceMessageTestSpec");
    }

    @Test
    public void extendedCleanUpClassNameTest() {
        DeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new DeviceMessageSpecPrimaryKey(ExtendedDeviceMessageTestWithoutOverriddenMethodSpec.TEST_SPEC_WITHOUT_SPECS, ExtendedDeviceMessageTestWithoutOverriddenMethodSpec.TEST_SPEC_WITHOUT_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(ExtendedDeviceMessageTestWithoutOverriddenMethodSpec.TEST_SPEC_WITHOUT_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.messages.DeviceMessageSpecPrimaryKeyTest$ExtendedDeviceMessageTestWithoutOverriddenMethodSpec");
    }

    @Test
    public void extendedCleanUpClassNameForSimpleEnumInnerClass(){
        DeviceMessageSpecPrimaryKey deviceMessageCategoryPrimaryKey = new DeviceMessageSpecPrimaryKey(ExtendedDeviceMessageTestWithOverriddenMethodSpec.TEST_SPEC_WITH_SIMPLE_SPECS, ExtendedDeviceMessageTestWithOverriddenMethodSpec.TEST_SPEC_WITH_SIMPLE_SPECS.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.cleanUpClassName(ExtendedDeviceMessageTestWithOverriddenMethodSpec.TEST_SPEC_WITH_SIMPLE_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.messages.DeviceMessageSpecPrimaryKeyTest$ExtendedDeviceMessageTestWithOverriddenMethodSpec");
    }

    public enum ExtendedDeviceMessageTestWithoutOverriddenMethodSpec implements DeviceMessageSpec {

        TEST_SPEC_WITH_SIMPLE_SPECS(PropertySpecFactory.bigDecimalPropertySpec("testMessageSpec.simpleBigDecimal"),
                PropertySpecFactory.stringPropertySpec("testMessageSpec.simpleString")),
        TEST_SPEC_WITH_EXTENDED_SPECS(PropertySpecFactory.codeTableReferencePropertySpec("testMessageSpec.codetable"),
                PropertySpecFactory.dateTimePropertySpec("testMessageSpec.activationdate")),
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

        @Override
        public long getMessageId() {
            return 0;
        }
    }

    public enum ExtendedDeviceMessageTestWithOverriddenMethodSpec implements DeviceMessageSpec {

        TEST_SPEC_WITH_SIMPLE_SPECS(PropertySpecFactory.bigDecimalPropertySpec("testMessageSpec.simpleBigDecimal"),
                PropertySpecFactory.stringPropertySpec("testMessageSpec.simpleString")) {
            @Override
            public void doSomeThing() {
                // doing something
            }
        },
        TEST_SPEC_WITH_EXTENDED_SPECS(PropertySpecFactory.codeTableReferencePropertySpec("testMessageSpec.codetable"),
                PropertySpecFactory.dateTimePropertySpec("testMessageSpec.activationdate")) {
            @Override
            public void doSomeThing() {
                // doing something
            }
        },
        TEST_SPEC_WITHOUT_SPECS {
            @Override
            public void doSomeThing() {
                // doing something
            }
        };

        private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.CONNECTIVITY_SETUP;

        private List<PropertySpec> deviceMessagePropertySpecs;

        ExtendedDeviceMessageTestWithOverriddenMethodSpec(PropertySpec... deviceMessagePropertySpecs) {
            this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
        }

        @Override
        public long getMessageId() {
            return 0;
        }

        public abstract void doSomeThing();

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
