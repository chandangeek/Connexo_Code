package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.List;

public class EventSetBuilderImpl implements CalendarService.EventSetBuilder {

    private final class EventBuilderImpl implements EventBuilder {
        private final String name;
        private int code;

        private EventBuilderImpl(String name) {
            this.name = name;
        }

        @Override
        public CalendarService.EventSetBuilder withCode(int code) {
            this.code = code;
            EventSetBuilderImpl.this.eventBuilders.add(this);
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

    @Override
    public EventBuilder addEvent(String name) {
        return new EventBuilderImpl(name);
    }

    @Override
    public EventSet add() {
        underConstruction.persist();
        eventBuilders.forEach(EventBuilderImpl::build);
        return underConstruction;
    }
}
