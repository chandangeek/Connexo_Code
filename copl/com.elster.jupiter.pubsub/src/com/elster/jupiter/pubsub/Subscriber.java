package com.elster.jupiter.pubsub;

public interface Subscriber {
	public static final String TOPIC = "TOPIC";
	
	void handle(Object event);
}
