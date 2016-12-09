package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.TariffCalender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Tests the {@link EnumBasedDeviceMessageSpecPrimaryKey} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/02/13
 * Time: 15:33
 */
public class EnumBasedDeviceMessageSpecPrimaryKeyTest {

    @Test
    public void correctNumberTest(){
        EnumBasedDeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new EnumBasedDeviceMessageSpecPrimaryKey(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS, DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric("12")).isTrue();
    }

    @Test
    public void incorrectNumberTest(){
        EnumBasedDeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new EnumBasedDeviceMessageSpecPrimaryKey(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS, DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric("I'mNotAnNumber :)")).isFalse();
    }

    @Test
    public void invalidNumberTest(){
        EnumBasedDeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new EnumBasedDeviceMessageSpecPrimaryKey(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS, DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.isGivenStringNumeric(null)).isFalse();
    }

    @Test
    public void cleanUpClassNameTest() {
        EnumBasedDeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new EnumBasedDeviceMessageSpecPrimaryKey(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS, DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.messages.DeviceMessageTestSpec");
    }

    @Test
    public void extendedCleanUpClassNameTest() {
        EnumBasedDeviceMessageSpecPrimaryKey deviceMessageSpecPrimaryKey = new EnumBasedDeviceMessageSpecPrimaryKey(ExtendedDeviceMessageTestWithoutOverriddenMethodSpec.TEST_SPEC_WITHOUT_SPECS, ExtendedDeviceMessageTestWithoutOverriddenMethodSpec.TEST_SPEC_WITHOUT_SPECS.name());

        // asserts
        assertThat(deviceMessageSpecPrimaryKey.cleanUpClassName(ExtendedDeviceMessageTestWithoutOverriddenMethodSpec.TEST_SPEC_WITHOUT_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.messages.EnumBasedDeviceMessageSpecPrimaryKeyTest$ExtendedDeviceMessageTestWithoutOverriddenMethodSpec");
    }

    @Test
    public void extendedCleanUpClassNameForSimpleEnumInnerClass(){
        EnumBasedDeviceMessageSpecPrimaryKey deviceMessageCategoryPrimaryKey = new EnumBasedDeviceMessageSpecPrimaryKey(ExtendedDeviceMessageTestWithOverriddenMethodSpec.TEST_SPEC_WITH_SIMPLE_SPECS, ExtendedDeviceMessageTestWithOverriddenMethodSpec.TEST_SPEC_WITH_SIMPLE_SPECS.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.cleanUpClassName(ExtendedDeviceMessageTestWithOverriddenMethodSpec.TEST_SPEC_WITH_SIMPLE_SPECS.getClass().getName())).isEqualTo("com.energyict.mdc.messages.EnumBasedDeviceMessageSpecPrimaryKeyTest$ExtendedDeviceMessageTestWithOverriddenMethodSpec");
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
        public DeviceMessageSpecPrimaryKey getPrimaryKey() {
            return new EnumBasedDeviceMessageSpecPrimaryKey(this, name());
        }

        @Override
        public long getMessageId() {
            return 0;
        }
    }

    public enum ExtendedDeviceMessageTestWithOverriddenMethodSpec implements DeviceMessageSpec {

        TEST_SPEC_WITH_SIMPLE_SPECS {
            @Override
            public List<PropertySpec> getPropertySpecs() {
                return Arrays.asList(
                        this.bigDecimalSpec("testMessageSpec.simpleBigDecimal"),
                        this.stringSpec("testMessageSpec.simpleString")
                );
            }

            @Override
            public void doSomeThing() {
                // doing something
            }
        },
        TEST_SPEC_WITH_EXTENDED_SPECS {
            @Override
            public List<PropertySpec> getPropertySpecs() {
                return Arrays.asList(
                        this.tariffCalendarSpec("testMessageSpec.codetable"),
                        this.dateTimeSpec("testMessageSpec.activationdate")
                );
            }

            @Override
            public void doSomeThing() {
                // doing something
            }
        },
        TEST_SPEC_WITHOUT_SPECS {
            @Override
            public List<PropertySpec> getPropertySpecs() {
                return Collections.emptyList() ;
            }

            @Override
            public void doSomeThing() {
                // doing something
            }
        };


        @Override
        public long getMessageId() {
            return ordinal();
        }

        public abstract void doSomeThing();

        @Override
        public DeviceMessageCategory getCategory() {
            return DeviceMessageTestCategories.CONNECTIVITY_SETUP.get(Services.propertySpecService(), Services.nlsService());
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public TranslationKey getNameTranslationKey() {
            return new NotSupported(this.name());
        }

        protected PropertySpec stringSpec(String name) {
            return this.finish(Services.propertySpecService().stringSpec(), name);
        }

        protected PropertySpec bigDecimalSpec(String name) {
            return this.finish(Services.propertySpecService().bigDecimalSpec(), name);
        }

        protected PropertySpec tariffCalendarSpec(String name) {
            return this.finish(Services.propertySpecService().referenceSpec(TariffCalender.class.getName()), name);
        }

        protected PropertySpec dateTimeSpec(String name) {
            return this.finish(Services.propertySpecService().dateTimeSpec(), name);
        }

        private <T> PropertySpec finish(PropertySpecBuilderWizard.NlsOptions<T> nlsOptions, String name) {
            return nlsOptions
                    .named(name, "No support for translation of property names in unit testing")
                    .describedAs("No support for description of properties in unit testing")
                    .finish();
        }

        @Override
        public DeviceMessageSpecPrimaryKey getPrimaryKey() {
            return new PrimaryKey(this);
        }
    }

    private static final class PrimaryKey implements DeviceMessageSpecPrimaryKey {
        private final String enumValueName;

        private PrimaryKey(ExtendedDeviceMessageTestWithOverriddenMethodSpec enumValue) {
            this.enumValueName = enumValue.name();
        }

        @Override
        public String getValue() {
            return ExtendedDeviceMessageTestWithOverriddenMethodSpec.class.getName() + "#" + this.enumValueName;
        }
    }

    private static final class NotSupported implements TranslationKey {
        private final String key;

        private NotSupported(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return "No support for translation of property names in unit testing";
        }
    }

}