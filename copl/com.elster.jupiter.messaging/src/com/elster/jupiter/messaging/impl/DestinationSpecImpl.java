package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;

import com.elster.jupiter.messaging.*;
import com.elster.jupiter.util.time.UtcInstant;

import oracle.AQ.AQException;
import oracle.AQ.AQQueueTable;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQFactory;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;
import oracle.jms.AQjmsDestination;
import oracle.jms.AQjmsDestinationProperty;
import oracle.jms.AQjmsQueueConnectionFactory;
import oracle.jms.AQjmsSession;

public class DestinationSpecImpl implements DestinationSpec {	
	// persistent fields
	private String name;
	private String queueTableName;
	private boolean active;
	private int retryDelay;
	
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime; 
	@SuppressWarnings("unused")
	private String userName;
	
	// associations
	private QueueTableSpec queueTableSpec;
	private List<ConsumerSpec> consumers;
		
	@SuppressWarnings("unused")
	private DestinationSpecImpl() {		
	}
	
	public DestinationSpecImpl(QueueTableSpec queueTableSpec, String name, int retryDelay) {
		this.name = name;
		this.queueTableSpec = queueTableSpec;
		this.queueTableName = queueTableSpec.getName();			
		this.retryDelay = retryDelay;
	}
	
	@Override
	public QueueTableSpec getQueueTableSpec() {
		if (queueTableSpec == null) {
			queueTableSpec = Bus.getOrmClient().getQueueTableSpecFactory().get(queueTableName);
		}
		return queueTableSpec;
	}
	
	@Override
	public List<ConsumerSpec> getConsumers() {
		return getConsumers(true);
	}
	
	private List<ConsumerSpec> getConsumers(boolean protect) {
		if (consumers == null) {
			consumers = Bus.getOrmClient().getConsumerSpecFactory().find("destination",this);
		}
		return protect ? Collections.unmodifiableList(consumers) : consumers;
	}
	
	
	@Override
	public void activate() {
		try {
			doActivate();
		} catch (SQLException | JMSException | AQException ex) {
			throw new RuntimeException(ex);
		}
		active = true;
		Bus.getOrmClient().getDestinationSpecFactory().update(this,"active");
	}
	
	private void doActivate() throws SQLException, JMSException, AQException {
		try (Connection connection = Bus.getConnection()) {
			QueueConnection queueConnection = AQjmsQueueConnectionFactory.createQueueConnection(connection);
			try {
				queueConnection.start();
				AQjmsSession session = (AQjmsSession) queueConnection.createSession(true, Session.AUTO_ACKNOWLEDGE);		
				AQQueueTable aqQueueTable = ((QueueTableSpecImpl) getQueueTableSpec()).getAqQueueTable(session);
				AQjmsDestinationProperty props = new AQjmsDestinationProperty();
				props.setRetryInterval(retryDelay);
				Destination destination = getQueueTableSpec().isMultiConsumer() ?
					session.createTopic(aqQueueTable, name , props) :
					session.createQueue(aqQueueTable, name, props);			
				((AQjmsDestination) destination).start(session,true,true);
			} finally {
				queueConnection.close();
			}
		}
	}
	
	@Override
	public void deActivate() {
		try {
			doDeActivate();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}		
		active = false;
		Bus.getOrmClient().getDestinationSpecFactory().update(this,"active");
	}
	
	private String dropSql() {
		return "begin dbms_aqadm.stop_queue(?); dmsq_aqadm.drop_queue(?); end;";					
	}
	
	private void doDeActivate() throws SQLException  {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(dropSql())) {
				statement.setString(1, name);
				statement.setString(2, name);
				statement.executeQuery();				
			}
		}
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isTopic() {
		return getQueueTableSpec().isMultiConsumer();
	}
	
	@Override
	public boolean isQueue() {
		return !getQueueTableSpec().isMultiConsumer();
	}
	
	@Override
	public String getPayloadType() {
		// TODO Auto-generated method stub
		return getQueueTableSpec().getName();
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public void send(byte[]  bytes)  {
		try {
			doSend(bytes);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	void doSend(byte[] bytes) throws SQLException {
		AQMessageProperties props = AQFactory.createAQMessageProperties();
		AQMessage message = AQFactory.createAQMessage(props);
		message.setPayload(bytes);
		send(message);		
	}
	
	@Override
	public void send(String text) {
		send(text.getBytes());
	}
	
	@Override
	public void send(AQMessage message) throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			OracleConnection oraConnection= connection.unwrap(OracleConnection.class);		
			oraConnection.enqueue(name, new AQEnqueueOptions() , message);
		}
	}
	
	@Override
	public ConsumerSpec subscribe(String name , int workerCount) {
		List<ConsumerSpec> currentConsumers = getConsumers(false);
		for (ConsumerSpec each : currentConsumers) {
			if (each.getName().equals(name)) {
				throw new RuntimeException("Duplicate name");
			}
		}
		if (isQueue() && !currentConsumers.isEmpty()) {
			throw new RuntimeException("Queues can only have one suscriber");
		}
		ConsumerSpecImpl result = new ConsumerSpecImpl(this, name, workerCount);
		result.subscribe();
		Bus.getOrmClient().getConsumerSpecFactory().persist(result);
		return result;
	}
	
}
