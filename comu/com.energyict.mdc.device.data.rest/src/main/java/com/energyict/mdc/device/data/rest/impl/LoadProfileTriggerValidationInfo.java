/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;

public class LoadProfileTriggerValidationInfo {

    public long id;
    public String name;
    public Long lastChecked;
    public long version;
    public VersionInfo<String> parent;
}
