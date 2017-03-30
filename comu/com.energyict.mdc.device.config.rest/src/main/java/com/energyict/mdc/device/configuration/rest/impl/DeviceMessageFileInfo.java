/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceMessageFile;

public class DeviceMessageFileInfo {
    public String name;
    public long id;
    public long creationDate;

    public DeviceMessageFileInfo(DeviceMessageFile file) {
        this.name = file.getName();
        this.id = file.getId();
        this.creationDate = file.getCreateTime().toEpochMilli();
    }

    public DeviceMessageFileInfo() {

    }

}
