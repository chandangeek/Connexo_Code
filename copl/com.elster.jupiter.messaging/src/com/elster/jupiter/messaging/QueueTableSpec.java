package com.elster.jupiter.messaging;

public interface QueueTableSpec {
	String getName();
	void activate();
	void deActivate();
	String getPayloadType();
	boolean isMultiConsumer();
	boolean isActive();
	DestinationSpec createDestinationSpec(String name, int retryDelay);
}
