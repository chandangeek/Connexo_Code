package com.energyict.mdc.issue.datacollection.impl.templates;

import java.util.Arrays;
import java.util.Optional;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;

public class EventTypes implements CanFindByStringKey<EventTypes.EventType> {

    private final EventType[] eventTypes;
    private Thesaurus thesaurus;

    public EventTypes(Thesaurus thesaurus, DataCollectionEventDescription... eventTypes) {
        this.thesaurus = thesaurus;
        this.eventTypes = Arrays.asList(eventTypes).stream().map(EventType::new).toArray(EventType[]::new);
    }

    @Override
    public Optional<EventType> find(String key) {
        for (EventType eventType : eventTypes) {
            if (eventType.getId().equals(key)) {
                return Optional.of(eventType);
            }
        }
        return Optional.empty();
    }

    @Override
    public Class<EventType> valueDomain() {
        return EventType.class;
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
            return event.getTitle().getTranslated(thesaurus);
        }
    }
}