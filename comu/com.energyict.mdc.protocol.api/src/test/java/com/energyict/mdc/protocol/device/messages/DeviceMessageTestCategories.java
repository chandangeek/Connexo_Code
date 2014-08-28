package com.energyict.mdc.protocol.device.messages;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategoryPrimaryKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import java.util.Arrays;
import java.util.List;

/**
 * Test enum for DeviceMessageCategories.
 *
 * Copyrights EnergyICT
 * Date: 8/02/13
 * Time: 15:30
 */
public enum DeviceMessageTestCategories implements DeviceMessageCategory {

    FIRST_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

        }
    },
    SECOND_TEST_CATEGORY {
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
    },
    SECURITY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

        }
    },
    CONNECTIVITY_SETUP {
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
        return new DeviceMessageCategoryPrimaryKey(this, name());
    }
}
