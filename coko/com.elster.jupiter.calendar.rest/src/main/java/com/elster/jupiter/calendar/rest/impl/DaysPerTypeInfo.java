package com.elster.jupiter.calendar.rest.impl;

import java.util.List;

public class DaysPerTypeInfo {
    public long dayTypeId;
    public List<String> days;

    public DaysPerTypeInfo(long dayTypeId, List<String> days) {
        this.dayTypeId = dayTypeId;
        this.days = days;
    }
}
