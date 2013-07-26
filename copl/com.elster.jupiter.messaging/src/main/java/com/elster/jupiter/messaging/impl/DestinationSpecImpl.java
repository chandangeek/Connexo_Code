package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.time.UtcInstant;
import oracle.AQ.AQException;
import oracle.AQ.AQQueueTable;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQMessage;
import oracle.jms.AQjmsDestination;
import oracle.jms.AQjmsDestinationProperty;
import oracle.jms.AQjmsQueueConnectionFactory;
import oracle.jms.AQjmsSession;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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
	private List<SubscriberSpec> consumers;
		
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
			queueTableSpec = Bus.getOrmClient().getQueueTableSpecFactory().getExisting(queueTableName);
		}
		return queueTableSpec;
	}
	
	@Override
	public List<SubscriberSpec> getConsumers() {
		return getConsumers(true);
	}
	
	private List<SubscriberSpec> getConsumers(boolean protect) {
		if (consumers == null) {
			consumers = Bus.getOrmClient().getConsumerSpecFactory().find("destination",this);
		}
		return protect ? Collections.unmodifiableList(consumers) : consumers;
	}
	
	
	@Override
	public void activate() {
		try {
			if (getQueueTableSpec().isJms()) {
				doActivateJms();
			} else {
				doActivateAq();
			}
		} catch (SQLException | JMSException | AQException ex) {
			throw new RuntimeException(ex);
		}
		active = true;
		Bus.getOrmClient().getDestinationSpecFactory().update(this,"active");
	}
	
	private void doActivateAq() throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(createSql())) {
				statement.setString(1, name);
				statement.setString(2, queueTableName);
				statement.setInt(3, retryDelay);
				statement.setString(4, name);
				statement.executeQuery();				
			}
		}
	}
	
	private void doActivateJms() throws SQLException, JMSException, AQException {
		try (Connection connection = Bus.getConnection()) {
			OracleConnection oraConnection = connection.unwrap(OracleConnection.class);
			QueueConnection queueConnection = AQjmsQueueConnectionFactory.createQueueConnection(oraConnection);
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
	public void deactivate() {
		try {
			doDeactivate();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}		
		active = false;
		Bus.getOrmClient().getDestinationSpecFactory().update(this,"active");
	}
	
	
	private String createSql() {
		return "begin dbms_aqadm.create_queue(queue_name => ?, queue_table => ?, retry_delay => ?); dbms_aqadm.start_queue(?); end;";					
	}
	
	private String dropSql() {
		return "begin dbms_aqadm.stop_queue(?); dbms_aqadm.drop_queue(?); end;";
	}
	
	private void doDeactivate() throws SQLException  {
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
		return getQueueTableSpec().getPayloadType();
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public MessageBuilder message(byte[] bytes)  {
        return new BytesMessageBuilder(this, bytes);
	}
	
	@Override
	public MessageBuilder message(String text) {
		if (getQueueTableSpec().isJms()) {
			throw new RuntimeException("JMS support not yet implemented.");
		} else {
			return new BytesMessageBuilder(this, text.getBytes());
		}
	}
	
	@Override
	public MessageBuilder message(AQMessage message) {
        return new AQMessageOptionsBuilder(this, message);
	}
	
	@Override
	public SubscriberSpec subscribe(String name , int workerCount) {
		List<SubscriberSpec> currentConsumers = getConsumers(false);
		for (SubscriberSpec each : currentConsumers) {
			if (each.getName().equals(name)) {
				throw new RuntimeException("Duplicate name");
			}
		}
		if (isQueue() && !currentConsumers.isEmpty()) {
			throw new RuntimeException("Queues can only have one suscriber");
		}
		SubscriberSpecImpl result = new SubscriberSpecImpl(this, name);
		result.subscribe();
		Bus.getOrmClient().getConsumerSpecFactory().persist(result);
		return result;
	}

}
