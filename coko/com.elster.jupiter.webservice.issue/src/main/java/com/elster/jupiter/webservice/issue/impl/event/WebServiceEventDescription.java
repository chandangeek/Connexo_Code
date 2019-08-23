/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.event;

import org.osgi.service.event.EventConstants;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum WebServiceEventDescription {
    AUTH_FAILURE(WebServiceEvent.class, "com/elster/jupiter/webservices/fail/in/AUTH_FAILURE", "com/elster/jupiter/webservices/fail/out/AUTH_FAILURE"),
    ENDPOINT_NOT_AVAILABLE(WebServiceEvent.class, "com/elster/jupiter/webservices/fail/out/ENDPOINT_NOT_AVAILABLE"),
    NO_ACKNOWLEDGEMENT(WebServiceEvent.class, "com/elster/jupiter/webservices/fail/out/NO_ACKNOWLEDGEMENT"),
    BAD_ACKNOWLEDGEMENT(WebServiceEvent.class, "com/elster/jupiter/webservices/fail/out/BAD_ACKNOWLEDGEMENT");
    
    private final Set<String> topics;
    private final Class<? extends WebServiceEvent> eventClass;
    
    WebServiceEventDescription(Class<? extends WebServiceEvent> eventClass, String... topics) {
        this.topics = Arrays.stream(topics).collect(Collectors.toSet());
        this.eventClass = eventClass;
    }
    
    public Set<String> getTopics() {
        return topics;
    }
    
    public Class<? extends WebServiceEvent> getEventClass() {
        return eventClass;
    }
    
    public boolean matches(Map<?, ?> map) {
        String topic = (String) map.get(EventConstants.EVENT_TOPIC);
        return topics.contains(topic);
    }
}
