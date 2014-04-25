package com.energyict.mdc.masterdata.rest.impl;

import com.energyict.mdc.masterdata.LogBookType;

public class LogBookTypeInfo {
    public Long id;
    public String name;
    public String obis;

    public LogBookTypeInfo() {
    }

    public LogBookTypeInfo(LogBookType logbook) {
        this.id = logbook.getId();
        this.name = logbook.getName();
        this.obis = logbook.getObisCode().toString();
    }
}
