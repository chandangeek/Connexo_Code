/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import java.util.List;

public enum DeviceMessageTestCategories implements DeviceMessageCategory {

    FIRST_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return DeviceMessageTestSpec.allTestSpecs(getPropertySpecService());

        }
    },
    SECOND_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return DeviceMessageTestSpec.allTestSpecs(getPropertySpecService());

        }
    },
    THIRD_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return DeviceMessageTestSpec.allTestSpecs(getPropertySpecService());
        }
    },
    SECURITY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return DeviceMessageTestSpec.allTestSpecs(getPropertySpecService());
        }
    },
    CONNECTIVITY_SETUP {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return DeviceMessageTestSpec.allTestSpecs(getPropertySpecService());
        }
    };
    private PropertySpecService propertySpecService;

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


    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

}