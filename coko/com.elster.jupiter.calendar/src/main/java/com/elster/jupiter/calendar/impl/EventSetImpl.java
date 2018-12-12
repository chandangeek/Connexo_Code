/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventSetImpl implements EventSet {

    public enum Fields {
        ID("id"),
        NAME("name"),
        EVENTS("events");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private long id;

    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.EVENTSET_NAME_FIELD_TOO_LONG + "}")
    private String name;

    private final DataModel dataModel;

    @Valid
    private List<Event> events = new ArrayList<>();

    public static EventSetImpl from(DataModel dataModel, String name) {
        return dataModel.getInstance(EventSetImpl.class).init(name);
    }

    @Override
    public long getId() {
        return id;
    }

    @Inject
    public EventSetImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private EventSetImpl init(String name) {
        this.name = name;
        return this;
    }

    @Override
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public String getName() {
        return name;
    }

    void persist() {
        dataModel.persist(this);
    }

    Event addEvent(String name, long code) {
        EventImpl event = dataModel.getInstance(EventImpl.class).init(this, name, code);
        Save.CREATE.save(dataModel, event);
        this.events.add(event);
        return event;
    }

    @Override
    public CalendarService.EventSetBuilder redefine() {
        return new EventSetBuilderImpl(this);
    }
}
