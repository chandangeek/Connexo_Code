package com.elster.jupiter.messaging;

public interface MessagingService {
	QueueTable createQueueTable(String name, String payloadType , boolean multiConsumer);
}
