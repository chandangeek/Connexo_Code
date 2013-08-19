package com.elster.jupiter.pubsub;

/**
 * A Subscriber service handles events synchronously.
 */
public interface Subscriber {

    /**
     * Key of the Topic of the osgi subsriber property
     */
    String TOPIC = "TOPIC";
	
	void handle(Object event, Object... eventDetails);
}
