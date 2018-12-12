/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.impl.SearchHelperValueFactory;

import java.util.Arrays;

public class DeviceDataStatusValueFactory extends SearchHelperValueFactory<DeviceDataStatusSearchWrapper> {
    DeviceDataStatusValueFactory() {
        super(DeviceDataStatusSearchWrapper.class);
    }

    @Override
    public DeviceDataStatusSearchWrapper fromStringValue(String stringValue) {
        return Arrays.stream(DeviceDataStatusContainer.values())
                .filter(deviceDataStatusContainer -> deviceDataStatusContainer.getId().equalsIgnoreCase(stringValue))
                .map(DeviceDataStatusSearchWrapper::new)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toStringValue(DeviceDataStatusSearchWrapper deviceDataStatusSearchWrapper) {
        return deviceDataStatusSearchWrapper.getId();
    }
}
