package com.elster.jupiter.messaging;

public interface QueueTableSpec {
	String getName();
	void activate();
	void deactivate();
	String getPayloadType();
	boolean isMultiConsumer();
	boolean isActive();
	DestinationSpec createDestinationSpec(String name, int retryDelay);
	boolean isJms();
}
