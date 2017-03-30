/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class IntervalErrorInfos {
    public boolean success = false;
    public List<IntervalErrorInfo> errors = new ArrayList<>();

    public IntervalErrorInfos() {
        errors.add(new IntervalErrorInfo("startTime", MessageSeeds.INTERVAL_START_AFTER_END.getDefaultFormat()));
        errors.add(new IntervalErrorInfo("endTime", MessageSeeds.INTERVAL_END_BEFORE_START.getDefaultFormat()));
    }

    public class IntervalErrorInfo {
        public String id;
        public String msg;

        public IntervalErrorInfo(String id, String msg) {
            this.id = id;
            this.msg = msg;
        }
    }
}
