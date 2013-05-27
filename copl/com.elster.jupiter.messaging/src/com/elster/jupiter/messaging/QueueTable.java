package com.elster.jupiter.messaging;

public interface QueueTable {
	String getName();
	void activate();
	void deActivate();
	String getPayloadType();
	boolean isMultiConsumer();
	boolean isActive();
}
