/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.time.TimeDuration;

public class OutputIntervalInfo {
    public long id;
    public String name;
    public long count;
    public String timeUnit = TimeDuration.TimeUnit.SECONDS.getDescription();

    public OutputIntervalInfo() {
    }

    public OutputIntervalInfo(long id, String name, TimeDuration timeDuration) {
        this.id = id;
        this.name = name;
        this.count = timeDuration.getCount();
        this.timeUnit = TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
    }
}
