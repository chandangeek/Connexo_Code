package com.elster.jupiter.pubsub;

/**
 * A Subscriber service handles events synchronously.
 */
public interface Subscriber {	
	void handle(Object event, Object... eventDetails);
	Class<?>[] getClasses();
}
