package com.elster.jupiter.events;


import java.util.List;

import com.elster.jupiter.orm.TransactionRequired;
import com.google.common.base.Optional;

public interface EventService {

    String JUPITER_EVENTS = "JupiterEvents";

    void postEvent(String topic, Object source);

    @TransactionRequired
    EventTypeBuilder buildEventTypeWithTopic(String topic);
    
    List<EventType> getEventTypes();
    
    Optional<EventType> getEventType(String topic);
    
    
    

}
