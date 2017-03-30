/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;

public class ConnectionTaskVersionInfo {

    public long id;
    public String name;
    public long version;
    public VersionInfo<String> parent;
}
