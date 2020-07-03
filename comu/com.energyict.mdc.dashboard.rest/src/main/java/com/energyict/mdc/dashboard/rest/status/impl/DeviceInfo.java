/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

public class DeviceInfo extends IdWithNameInfo {

    public IdWithNameInfo location;

    public DeviceInfo(Object id, String name, IdWithNameInfo location) {
        super(id, name);
        this.location = location;
    }
}
