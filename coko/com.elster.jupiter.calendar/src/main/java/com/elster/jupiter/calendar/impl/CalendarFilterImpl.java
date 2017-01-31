/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarFilter;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

public class CalendarFilterImpl implements CalendarFilter {

    private Status status;
    private Category category;

    @Override
    public CalendarFilter setStatus(Status status) {
        this.status = status;
        return this;
    }

    @Override
    public CalendarFilter setCategory(Category category) {
        this.category = category;
        return this;
    }

    @Override
    public Condition toCondition() {
        Condition condition = Condition.TRUE;
        if (status != null) {
            condition = condition.and(Operator.EQUAL.compare(CalendarImpl.Fields.STATUS.fieldName(), status));
        }
        if (category != null) {
            condition = condition.and(Operator.EQUAL.compare(CalendarImpl.Fields.CATEGORY.fieldName(), category));
        }
        return condition;
    }
}
