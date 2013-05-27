package com.elster.jupiter.messaging;

public interface MessagingService {
	QueueTableSpec createQueueTableSpec(String name, String payloadType , boolean multiConsumer);
	QueueTableSpec getQueueTableSpec(String name);
	DestinationSpec getDestinationSpec(String name);
}
