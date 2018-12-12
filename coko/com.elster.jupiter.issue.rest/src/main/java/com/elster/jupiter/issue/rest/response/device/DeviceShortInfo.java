/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.EndDevice;

public class DeviceShortInfo extends DeviceInfo {

    public DeviceShortInfo(EndDevice endDevice) {
        super(endDevice);
    }

    @Override
    protected void fetchDetails(EndDevice endDevice) {
        // No additional info is required
    }
}
