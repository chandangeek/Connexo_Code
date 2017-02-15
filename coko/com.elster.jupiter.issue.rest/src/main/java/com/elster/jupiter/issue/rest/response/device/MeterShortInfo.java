/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.EndDevice;

public class MeterShortInfo {
    public long id;
    public String name;

    public MeterShortInfo(EndDevice meter) {
        if (meter != null) {
            this.id = meter.getId();
            this.name = meter.getName();
        }
    }
}
