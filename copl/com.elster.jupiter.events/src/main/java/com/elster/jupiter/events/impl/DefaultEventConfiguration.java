package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;

class DefaultEventConfiguration implements EventConfiguration {

    @Override
    public String getEventDestinationName() {
        return EventService.JUPITER_EVENTS;
    }
}
