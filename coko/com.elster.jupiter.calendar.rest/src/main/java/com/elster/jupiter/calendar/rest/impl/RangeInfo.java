/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

public class RangeInfo {
    public long fromHour;
    public long fromMinute;
    public long fromSecond;
    public long event;

    public RangeInfo(long fromHour, long fromMinute, long fromSecond, long event) {
        this.fromHour = fromHour;
        this.fromMinute = fromMinute;
        this.fromSecond = fromSecond;
        this.event = event;
    }

    public RangeInfo(){}
}
