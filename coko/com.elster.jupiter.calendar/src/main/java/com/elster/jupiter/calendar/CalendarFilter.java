/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.util.conditions.Condition;

public interface CalendarFilter {
    CalendarFilter setStatus(Status status);

    CalendarFilter setCategory(Category category);

    Condition toCondition();
}
