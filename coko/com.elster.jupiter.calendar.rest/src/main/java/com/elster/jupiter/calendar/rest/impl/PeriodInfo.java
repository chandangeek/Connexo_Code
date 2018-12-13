/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.Period;

public class PeriodInfo {
    public String name;
    public long fromMonth;
    public long fromDay;

    public PeriodInfo() {

    }

    public PeriodInfo(String name, long fromMonth, long fromDay) {
        this.name = name;
        this.fromMonth = fromMonth;
        this.fromDay = fromDay;
    }
}
