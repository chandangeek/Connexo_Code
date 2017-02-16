/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;

/**
 * Created by bvn on 10/24/14.
 */
public class DeviceMessageCategoryInfoFactory {

    public DeviceMessageCategoryInfo asInfo(DeviceMessageCategory category) {
        DeviceMessageCategoryInfo info = new DeviceMessageCategoryInfo();
        info.id = category.getId();
        info.name = category.getName();

        return info;
    }

}
