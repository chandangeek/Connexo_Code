package com.elster.jupiter.messaging;

import java.sql.SQLException;
import java.util.List;

import oracle.jdbc.aq.AQMessage;

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
	void send(byte[] bytes);
	void send(AQMessage message) throws SQLException;
	List<ConsumerSpec> getConsumers();
	ConsumerSpec subscribe(String name, int workerCount);	
}
