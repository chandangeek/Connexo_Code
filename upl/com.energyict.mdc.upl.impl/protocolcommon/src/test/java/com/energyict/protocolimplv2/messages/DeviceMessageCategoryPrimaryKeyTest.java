package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageCategoryPrimaryKey;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Tests the {@link EnumBasedDeviceMessageCategoryPrimaryKey} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/02/13
 * Time: 8:49
 */
public class DeviceMessageCategoryPrimaryKeyTest {

    @Test
    public void correctNumberTest(){
        DeviceMessageCategoryPrimaryKey deviceMessageCategoryPrimaryKey = new EnumBasedDeviceMessageCategoryPrimaryKey(DeviceMessageTestCategories.SECURITY, DeviceMessageTestCategories.SECURITY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.isGivenStringNumeric("12")).isTrue();
    }

    @Test
    public void incorrectNumberTest(){
        DeviceMessageCategoryPrimaryKey deviceMessageCategoryPrimaryKey = new EnumBasedDeviceMessageCategoryPrimaryKey(DeviceMessageTestCategories.SECURITY, DeviceMessageTestCategories.SECURITY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.isGivenStringNumeric("I'mNotAnNumber :)")).isFalse();
    }

    @Test
    public void invalidNumberTest(){
        DeviceMessageCategoryPrimaryKey deviceMessageCategoryPrimaryKey = new EnumBasedDeviceMessageCategoryPrimaryKey(DeviceMessageTestCategories.SECURITY, DeviceMessageTestCategories.SECURITY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.isGivenStringNumeric(null)).isFalse();
    }

    @Test
    public void cleanUpClassNameTest() {
        DeviceMessageCategoryPrimaryKey deviceMessageCategoryPrimaryKey = new EnumBasedDeviceMessageCategoryPrimaryKey(DeviceMessageTestCategories.SECURITY, DeviceMessageTestCategories.SECURITY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.cleanUpClassName(DeviceMessageTestCategories.SECURITY.getClass().getName())).isEqualTo("com.energyict.mdc.messages.DeviceMessageTestCategories");
    }

    @Test
    public void extendedCleanUpClassNameTest() {
        DeviceMessageCategoryPrimaryKey deviceMessageCategoryPrimaryKey = new EnumBasedDeviceMessageCategoryPrimaryKey(ExtendedCategoriesForTestingWithOverriddenMethod.ACTIVITY_CALENDAR_FOR_TESTING, ExtendedCategoriesForTestingWithOverriddenMethod.ACTIVITY_CALENDAR_FOR_TESTING.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.cleanUpClassName(ExtendedCategoriesForTestingWithOverriddenMethod.ACTIVITY_CALENDAR_FOR_TESTING.getClass().getName())).isEqualTo("com.energyict.mdc.messages.DeviceMessageCategoryPrimaryKeyTest$ExtendedCategoriesForTestingWithOverriddenMethod");
    }

    @Test
    public void extendedCleanUpClassNameForSimpleEnumInnerClass(){
        DeviceMessageCategoryPrimaryKey deviceMessageCategoryPrimaryKey = new EnumBasedDeviceMessageCategoryPrimaryKey(ExtendedCategoriesForTestingWithoutOverriddenMethod.THIRD_TEST_CATEGORY, ExtendedCategoriesForTestingWithoutOverriddenMethod.THIRD_TEST_CATEGORY.name());

        // asserts
        assertThat(deviceMessageCategoryPrimaryKey.cleanUpClassName(ExtendedCategoriesForTestingWithoutOverriddenMethod.THIRD_TEST_CATEGORY.getClass().getName())).isEqualTo("com.energyict.mdc.messages.DeviceMessageCategoryPrimaryKeyTest$ExtendedCategoriesForTestingWithoutOverriddenMethod");
    }

    private enum ExtendedCategoriesForTestingWithOverriddenMethod implements DeviceMessageCategory {

        FOR_TESTING {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

            }
        },
        ACTIVITY_CALENDAR_FOR_TESTING {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

            }
        },
        THIRD_TEST_CATEGORY {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

            }
        };

        @Override
        public String getName() {
            return this.getNameResourceKey();
        }

        /**
         * Gets the resource key that determines the name
         * of this category to the user's language settings.
         *
         * @return The resource key
         */
        public String getNameResourceKey() {
            return DeviceMessageTestCategories.class.getSimpleName() + "." + this.toString();
        }

        @Override
        public String getDescription() {
            return this.getDescriptionResourceKey();
        }

        /**
         * Gets the resource key that determines the description
         * of this category to the user's language settings.
         *
         * @return The resource key
         */
        private String getDescriptionResourceKey() {
            return this.getNameResourceKey() + ".description";
        }

        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        public void setXmlType(String ignore) {
            // For xml unmarshalling purposes only
        }

        @Override
        public int getId() {
            return this.ordinal();
        }

        @Override
        public abstract List<DeviceMessageSpec> getMessageSpecifications();

        @Override
        public DeviceMessageCategoryPrimaryKey getPrimaryKey() {
            return new EnumBasedDeviceMessageCategoryPrimaryKey(this, name());
        }
    }

    private enum ExtendedCategoriesForTestingWithoutOverriddenMethod implements DeviceMessageCategory {

        FOR_TESTING,
        ACTIVITY_CALENDAR_FOR_TESTING,
        THIRD_TEST_CATEGORY;

        @Override
        public String getName() {
            return this.getNameResourceKey();
        }

        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        public void setXmlType(String ignore) {
            // For xml unmarshalling purposes only
        }

        /**
         * Gets the resource key that determines the name
         * of this category to the user's language settings.
         *
         * @return The resource key
         */
        public String getNameResourceKey() {
            return DeviceMessageTestCategories.class.getSimpleName() + "." + this.toString();
        }

        @Override
        public String getDescription() {
            return this.getDescriptionResourceKey();
        }

        /**
         * Gets the resource key that determines the description
         * of this category to the user's language settings.
         *
         * @return The resource key
         */
        private String getDescriptionResourceKey() {
            return this.getNameResourceKey() + ".description";
        }

        @Override
        public int getId() {
            return this.ordinal();
        }

        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Collections.emptyList();
        }


        @Override
        public DeviceMessageCategoryPrimaryKey getPrimaryKey() {
            return new EnumBasedDeviceMessageCategoryPrimaryKey(this, name());
        }
    }
}
