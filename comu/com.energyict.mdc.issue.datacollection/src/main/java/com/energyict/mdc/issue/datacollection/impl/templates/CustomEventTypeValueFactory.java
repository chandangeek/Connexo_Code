package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.properties.rest.CustomEventTypePropertyFactory;

public class CustomEventTypeValueFactory extends EventTypeValueFactory implements CustomEventTypePropertyFactory {

    public CustomEventTypeValueFactory(EventTypes eventTypes) {
        super(eventTypes);
    }

}
