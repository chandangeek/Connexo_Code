/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;

public class DeviceVersionInfo {

    public String name;
    public long version;
    public VersionInfo<Long> parent;
}
