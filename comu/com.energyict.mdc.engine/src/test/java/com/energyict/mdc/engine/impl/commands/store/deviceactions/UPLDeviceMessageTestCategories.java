/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import java.util.Arrays;
import java.util.List;

public enum UPLDeviceMessageTestCategories implements DeviceMessageCategory {

    FIRST_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(UPLDeviceMessageTestSpec.values());
        }
    },
    SECOND_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(UPLDeviceMessageTestSpec.values());

        }
    },
    THIRD_TEST_CATEGORY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(UPLDeviceMessageTestSpec.values());

        }
    };

    @Override
    public String getName() {
        return "DeviceMessageTestCategories";
    }

    @Override
    public String getNameResourceKey() {
        return name();
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