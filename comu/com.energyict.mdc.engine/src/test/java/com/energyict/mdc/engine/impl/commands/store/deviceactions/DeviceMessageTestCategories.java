package com.energyict.mdc.engine.impl.commands.store.deviceactions;


import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import java.util.Arrays;
import java.util.List;

/**
 * Test enum for DeviceMessageCategories
 * <p>
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
    };

    @Override
    public String getName() {
        return "DeviceMessageTestCategories";
    }

    @Override
    public String getDescription() {
        return "DeviceMessageTestCategories";
    }

    @Override
    public int getId() {
        return this.ordinal();
    }
}