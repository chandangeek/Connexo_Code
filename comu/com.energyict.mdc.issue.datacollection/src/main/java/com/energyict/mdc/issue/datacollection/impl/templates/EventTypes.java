package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;

import java.util.Arrays;

public class EventTypes {

    private final EventType[] eventTypes;
    private Thesaurus thesaurus;

    public EventTypes(Thesaurus thesaurus, DataCollectionEventDescription... eventTypes) {
        this.thesaurus = thesaurus;
        this.eventTypes = Arrays.asList(eventTypes).stream().map(EventType::new).toArray(EventType[]::new);
    }

    public EventType[] getEventTypes() {
        return eventTypes;
    }

    public class EventType extends HasIdAndName {

        private DataCollectionEventDescription event;

        public EventType(DataCollectionEventDescription event) {
            this.event = event;
        }

        @Override
        public String getId() {
            return event.getUniqueKey();
        }

        @Override
        public String getName() {
            return thesaurus.getString(event.getTitle().getKey(), event.getTitle().getDefaultFormat());
        }
    }
}