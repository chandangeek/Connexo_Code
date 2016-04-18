package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.Size;

/**
 * Created by igh on 15/04/2016.
 */
public class CategoryImpl implements Category {

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;

    private final CalendarService calendarService;

    @Inject
    CategoryImpl(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }


}
