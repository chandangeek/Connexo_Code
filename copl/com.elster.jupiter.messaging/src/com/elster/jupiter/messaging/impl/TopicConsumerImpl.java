package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQMessage;

import com.elster.jupiter.messaging.TopicConsumer;

public class TopicConsumerImpl implements TopicConsumer {
	
	private final DestinationSpecImpl destinationSpec;
	private final String consumerName;

	TopicConsumerImpl(DestinationSpecImpl spec, String consumerName) {
		this.destinationSpec = spec;
		this.consumerName = consumerName;
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
			options.setConsumerName(consumerName);
			AQMessage message = oraConnection.dequeue(destinationSpec.getName(), options , "RAW");
			return new String(message.getPayload());
		}
	}
	
	@Override
	public void subscribe() {
		try {
			doSubscribe();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	void doSubscribe() throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(subscribeSql())) {
				statement.setString(1, consumerName);
				statement.setString(2, destinationSpec.getName());
				statement.executeUpdate();
			}			
		}
	}
	
	@Override
	public void unSubscribe() {
		try {
			doUnSubscribe(consumerName);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	void doUnSubscribe(String consumerName) throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(unSubscribeSql())) {
				statement.setString(1, consumerName);
				statement.setString(2, destinationSpec.getName());
				statement.executeUpdate();
			}			
		}
	}
	private String subscribeSql() {
		return 
			"declare subscriber sys.aq$_agent;  " +
			"begin subscriber := sys.aq$_agent(?,null,null); + " +
			"dbms_aqadm.add_subscriber(?,subscriber); end;";
		
				
	}
	
	private String unSubscribeSql() {
		return 
			"declare subscriber sys.aq$_agent;  " +
			"begin subscriber := sys.aq$_agent(?,null,null); + " +
			"dbms_aqadm.remove_subscriber(?,subscriber); end;";		
	}
	
}
