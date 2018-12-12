/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

import java.util.List;

public class DaysPerTypeInfo {
    public long dayTypeId;
    public List<String> days;

    public DaysPerTypeInfo () {

    }

    public DaysPerTypeInfo(long dayTypeId, List<String> days) {
        this.dayTypeId = dayTypeId;
        this.days = days;
    }
}
