/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.List;

class EventSetBuilderImpl implements CalendarService.EventSetBuilder {

    private final class EventBuilderImpl implements EventBuilder {
        private final String name;
        private int code;

        private EventBuilderImpl(String name) {
            this.name = name;
        }

        @Override
        public CalendarService.EventSetBuilder withCode(int code) {
            if (underConstruction.getEvents().stream().noneMatch(event -> event.getName().equals(name))) {
                this.code = code;
                EventSetBuilderImpl.this.eventBuilders.add(this);
            }
            return EventSetBuilderImpl.this;
        }

        public void build() {
            underConstruction.addEvent(name, code);
        }
    }

    private EventSetImpl underConstruction;
    private List<EventBuilderImpl> eventBuilders = new ArrayList<>();

    public EventSetBuilderImpl(DataModel dataModel, String name) {
        underConstruction = EventSetImpl.from(dataModel, name);
    }

    EventSetBuilderImpl(EventSetImpl existing) {
        this.underConstruction = existing;
    }

    @Override
    public EventBuilder addEvent(String name) {
        return new EventBuilderImpl(name);
    }

    @Override
    public EventSet add() {
        save();
        eventBuilders.forEach(EventBuilderImpl::build);
        return underConstruction;
    }

    void save() {
        if (underConstruction.getId() == 0) {
            underConstruction.persist();
        }
    }
}
