package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQFactory;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;

public class TopicImpl {
	
	private String name = "testtopic";

	void send(String text) throws SQLException {
		AQMessageProperties props = AQFactory.createAQMessageProperties();
		AQMessage message = AQFactory.createAQMessage(props);
		message.setPayload(text.getBytes());
		try (Connection connection = Bus.getConnection()) {				
			OracleConnection oraConnection = connection.unwrap(OracleConnection.class);
			oraConnection.enqueue(name, new AQEnqueueOptions() , message);
		}
	}
	
	String receive() throws SQLException {
		try (Connection connection = Bus.getConnection()) {					
			OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
			AQDequeueOptions options = new AQDequeueOptions();
			options.setConsumerName("SUB2");
			AQMessage message = oraConnection.dequeue(name, options , "RAW");
			return new String(message.getPayload());
		}
	}
	
	void startReceivers() {
		for (int i = 0 ; i < 2 ; i++) {
			Runnable runnable = new Runnable() {				
				@Override
				public void run() {
					for(;;) {
						try {
							System.out.println("" + Thread.currentThread().getId() + ":" + receive());
						} catch (SQLException ex) {
							ex.printStackTrace();
						}
					}					
				}
			};
			new Thread(runnable).start();
		}
	}
}
