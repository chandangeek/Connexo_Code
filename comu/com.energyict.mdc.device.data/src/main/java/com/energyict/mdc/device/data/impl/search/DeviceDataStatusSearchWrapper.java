/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.properties.HasIdAndName;

public class DeviceDataStatusSearchWrapper extends HasIdAndName {
    private final DeviceDataStatusContainer deviceDataStatusContainer;

    DeviceDataStatusSearchWrapper(DeviceDataStatusContainer deviceDataStatusContainer) {
        this.deviceDataStatusContainer = deviceDataStatusContainer;
    }

    @Override
    public String getId() {
        return deviceDataStatusContainer.name();
    }

    @Override
    public String getName() {
        return deviceDataStatusContainer.getTranslation().getKey();
    }

    public DeviceDataStatusContainer getDeviceDataStatusContainer() {
        return this.deviceDataStatusContainer;
    }

}
