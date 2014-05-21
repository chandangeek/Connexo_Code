package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.Meter;

public class MeterShortInfo {
    private long id;
    private String name;

    public MeterShortInfo(Meter meter) {
        if (meter != null) {
            this.id = meter.getId();
            this.name = meter.getMRID();
        }
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
