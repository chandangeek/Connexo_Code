package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.Meter;

public class MeterShortInfo {
    private long id;
    private String name;

    public MeterShortInfo(Meter meter) {
        this.id = meter.getId();
        String displayedString = meter.getName() + " " + meter.getSerialNumber();
        this.name = displayedString;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
