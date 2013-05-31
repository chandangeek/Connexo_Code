package com.elster.jupiter.messaging;

public interface MessageService {
	QueueTableSpec createQueueTableSpec(String name, String payloadType , boolean multiConsumer);
	QueueTableSpec getQueueTableSpec(String name);
	DestinationSpec getDestinationSpec(String name);
}
