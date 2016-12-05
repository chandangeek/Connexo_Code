package com.energyict.mdc.upl.messages;

import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.List;

/**
 * Test enum for DeviceMessageCategories
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/02/13
 * Time: 15:30
 */
public enum DeviceMessageTestCategories implements DeviceMessageCategory {

    CONNECTIVITY_SETUP {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.asList(DeviceMessageTestSpec.values());

        }
    },
    SECURITY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.asList(DeviceMessageTestSpec.values());

        }
    },
    THIRD_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.asList(DeviceMessageTestSpec.values());

        }
    };

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

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
    private String getNameResourceKey() {
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
    public abstract List<DeviceMessageSpec> getMessageSpecifications();

    @Override
    public DeviceMessageCategoryPrimaryKey getPrimaryKey() {
        return new EnumBasedDeviceMessageCategoryPrimaryKey(this, name());
    }
}
