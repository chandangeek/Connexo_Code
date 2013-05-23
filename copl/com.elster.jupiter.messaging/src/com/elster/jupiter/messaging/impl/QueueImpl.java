package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQFactory;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;

public class QueueImpl {
	
	private String name = "testqueue";

	void send(String text) throws SQLException {
		AQMessageProperties props = AQFactory.createAQMessageProperties();
		AQMessage message = AQFactory.createAQMessage(props);
		//message.setPayload(text.getBytes());
		try (Connection connection = Bus.getConnection()) {
			OracleConnection oraConnection= connection.unwrap(OracleConnection.class);		
			oraConnection.enqueue("testqueue", new AQEnqueueOptions() , message);
		}
	}
	
	String receive() throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
			AQDequeueOptions options = new AQDequeueOptions();
			AQMessage message = oraConnection.dequeue(name, options , "RAW");
			return new String(message.getPayload());
		}
	}
}
