package com.elster.jupiter.pubsub;

public interface Subscriber {

    /**
     * Key of the Topic of the osgi subsriber property
     */
	public static final String TOPIC = "TOPIC";
	
	void handle(Object event);
}
