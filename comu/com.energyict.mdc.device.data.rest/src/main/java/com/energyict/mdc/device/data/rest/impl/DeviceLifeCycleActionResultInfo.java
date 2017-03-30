/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.List;

public class DeviceLifeCycleActionResultInfo {
    public boolean result = true; // default = true, don't change to 'success'!
    public String targetState;
    public String message;
    public Instant effectiveTimestamp;
    public List<IdWithNameInfo> microChecks;

    public DeviceLifeCycleActionResultInfo() {}


}
