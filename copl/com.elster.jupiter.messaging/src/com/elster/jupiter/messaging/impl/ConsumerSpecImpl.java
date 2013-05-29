package com.elster.jupiter.messaging.impl;

import java.sql.*;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.*;

import com.elster.jupiter.messaging.ConsumerSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.consumer.MessageHandlerFactory;
import com.elster.jupiter.util.time.UtcInstant;

public class ConsumerSpecImpl implements ConsumerSpec {
	private String name;
	private String destinationName;
	private int workerCount;
	
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime; 
	@SuppressWarnings("unused")
	private String userName;
	
	private DestinationSpec destination;
	
	@SuppressWarnings("unused")
	private ConsumerSpecImpl() {
	}
	
	ConsumerSpecImpl(DestinationSpec destination, String name , int workerCount) {
		this.destination = destination;
		this.destinationName = destination.getName();
		this.name = name;
		this.workerCount = workerCount;
	}
	
	@Override
	public DestinationSpec getDestination() {
		if (destination == null) {
			destination = Bus.getOrmClient().getDestinationSpecFactory().get(destinationName);
		}
		return destination;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public int getWorkerCount() {
		return workerCount;
	}

		
	AQMessage receive() throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
			AQDequeueOptions options = new AQDequeueOptions();
			if (getDestination().isTopic()) {
				options.setConsumerName(name);
			}
			return oraConnection.dequeue(destinationName, options , getDestination().getPayloadType());			
		}
	}
	
	void subscribe() {
		try {
			doSubscribe();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	void doSubscribe() throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(subscribeSql())) {
				statement.setString(1, name);
				statement.setString(2, destinationName);
				statement.executeUpdate();
			}			
		}
	}
	
	public void unSubscribe() {
		try {
			doUnSubscribe(name);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	void doUnSubscribe(String consumerName) throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(unSubscribeSql())) {
				statement.setString(1, consumerName);
				statement.setString(2, destinationName);
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
	
	void start(MessageHandlerFactory factory) {
		// TO DO
	}
}

