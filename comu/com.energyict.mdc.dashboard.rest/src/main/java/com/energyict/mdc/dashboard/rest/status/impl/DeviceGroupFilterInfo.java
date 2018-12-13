/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

/**
 * Created by bvn on 11/17/15.
 */
public class DeviceGroupFilterInfo {
    public Object id;
    public String name;
    public String alias = FilterOption.deviceGroups.name(); // Should be in JSON answer

    public DeviceGroupFilterInfo() {
    }

    public DeviceGroupFilterInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
