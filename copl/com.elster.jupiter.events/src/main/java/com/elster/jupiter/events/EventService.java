package com.elster.jupiter.events;


import com.elster.jupiter.orm.TransactionRequired;
import com.google.common.base.Optional;

import java.util.List;

public interface EventService {

    String JUPITER_EVENTS = "JupiterEvents";
    String COMPONENTNAME = "EVT";

    void postEvent(String topic, Object source);

    @TransactionRequired
    EventTypeBuilder buildEventTypeWithTopic(String topic);
    
    List<EventType> getEventTypes();
    
    Optional<EventType> getEventType(String topic);
    
    
    

}
