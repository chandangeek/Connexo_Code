package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class IntervalErrorInfos {
    public boolean success = false;
    public List<IntervalErrorInfo> errors = new ArrayList<>();

    public IntervalErrorInfos() {
        errors.add(new IntervalErrorInfo("startTime", MessageSeeds.INTERVAL_START_EXCEEDS_END.getDefaultFormat()));
        errors.add(new IntervalErrorInfo("endTime", MessageSeeds.INTERVAL_END_BENEATH_START.getDefaultFormat()));
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
