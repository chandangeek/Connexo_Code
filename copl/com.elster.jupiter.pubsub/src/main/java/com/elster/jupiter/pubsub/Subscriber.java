package com.elster.jupiter.pubsub;

public interface Subscriber {

    /**
     * Key of the Topic of the osgi subsriber property
     */
    String TOPIC = "TOPIC";
	
	void handle(Object event, Object... eventDetails);
}
