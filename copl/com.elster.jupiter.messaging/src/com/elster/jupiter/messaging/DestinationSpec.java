package com.elster.jupiter.messaging;

public interface DestinationSpec {
	QueueTableSpec getQueueTableSpec();
	void activate();
	void deActivate();
	String getName();
	boolean isTopic();
	boolean isQueue();
	String getPayloadType();
	boolean isActive();
	void send(String text);	
	QueueConsumer asQueueConsumer();
	TopicConsumer asTopicConsumer(String consumerName);	
}
