package com.elster.jupiter.events;

import com.elster.jupiter.orm.TransactionRequired;

public interface EventService {

    String JUPITER_EVENTS = "JupiterEvents";

    void postEvent(String topic, Object source);

    @TransactionRequired
    EventTypeBuilder buildEventTypeWithTopic(String topic);

}
