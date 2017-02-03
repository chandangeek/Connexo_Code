/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;


import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.TransactionRequired;
import java.util.Optional;

import java.util.List;

@ProviderType
public interface EventService {

    String JUPITER_EVENTS = "JupiterEvents";
    String COMPONENTNAME = "EVT";

    /*
     * Posts an event with the given topic and source
     * This will wrap the source in a LocalEvent using the EventType defined by the topic
     * This event is first published on com.elster.jupiter.pubsub.Publisher,
     * then posted on the Osgi Event Admin Service and finally,
     * if the EventType is configured to publish, written as JSON payload on a Oracle AQ topic
     *  
     */
    void postEvent(String topic, Object source);

    @TransactionRequired
    EventTypeBuilder buildEventTypeWithTopic(String topic);

    List<EventType> getEventTypes();

    List<EventType> getEventTypesForComponent(String component);

    Optional<EventType> getEventType(String topic);

    Optional<EventType> findAndLockEventTypeByNameAndVersion(String topic, long version);

}
