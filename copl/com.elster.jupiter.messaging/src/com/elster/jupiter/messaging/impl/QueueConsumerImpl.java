package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQMessage;

import com.elster.jupiter.messaging.QueueConsumer;

public class QueueConsumerImpl implements QueueConsumer {
	
	private final DestinationSpecImpl destinationSpec;

	QueueConsumerImpl(DestinationSpecImpl spec) {
		this.destinationSpec = spec;
	}
	
	@Override
	public String receive() {
		try {
			return doReceive();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
		
	String doReceive() throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
			AQDequeueOptions options = new AQDequeueOptions();
			AQMessage message = oraConnection.dequeue(destinationSpec.getName(), options , "RAW");
			return new String(message.getPayload());
		}
	}

}
