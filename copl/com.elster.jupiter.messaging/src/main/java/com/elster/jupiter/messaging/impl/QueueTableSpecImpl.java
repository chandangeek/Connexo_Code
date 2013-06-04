package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.SQLException;

import javax.jms.*;

import oracle.AQ.*;
import oracle.jdbc.OracleConnection;
import oracle.jms.*;


import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.util.time.UtcInstant;


public class QueueTableSpecImpl implements QueueTableSpec {	
	// persistent fields
	private String name;
	private String payloadType;
	private boolean multiConsumer;
	private boolean active;
	
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime; 
	@SuppressWarnings("unused")
	private String userName;
	
	@SuppressWarnings("unused")
	private QueueTableSpecImpl() {		
	}
	
	public QueueTableSpecImpl(String name , String payloadType , boolean multiConsumer) {
		this.name = name;
		this.payloadType = payloadType;
		this.multiConsumer = multiConsumer;	
	}
	
	@Override
	public void activate()  {
		try {
			if(isJms()) {
				doActivateJms();
			} else {
				doActivateAq();
			}
		} catch (SQLException | AQException | JMSException ex) {
			throw new RuntimeException(ex);
		}
		active = true;
		Bus.getOrmClient().getQueueTableSpecFactory().update(this,"active");
	}
	
	private void doActivateAq() throws SQLException  {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(createSql())) {
				statement.setString(1, name);
				statement.setString(2, payloadType);
				statement.executeQuery();				
			}		
		}
	}
	
	private void doActivateJms() throws SQLException, JMSException, AQException {
		try (Connection connection = Bus.getConnection()) {
			System.out.println("In activate JMS");
			OracleConnection oraConnection = connection.unwrap(OracleConnection.class); 
			QueueConnection queueConnection = AQjmsQueueConnectionFactory.createQueueConnection(oraConnection);
			try {
				queueConnection.start();
				AQjmsSession session = (AQjmsSession) queueConnection.createSession(true, Session.AUTO_ACKNOWLEDGE);		
				AQQueueTableProperty properties = new AQQueueTableProperty(payloadType);
				properties.setMultiConsumer(multiConsumer);			
				session.createQueueTable("kore",name,properties);
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
		Bus.getOrmClient().getQueueTableSpecFactory().update(this,"active");
	}
	
	private String createSql() {
		return 
			"begin dbms_aqadm.create_queue_table(queue_table => ?, queue_payload_type => ? , multiple_consumers => " +
			(multiConsumer ? "TRUE" : "FALSE") + 
			"); end;";
	}
	
	private String dropSql() {
		return "begin dbms_aqadm.drop_queue_table(?); end;";
		
				
	}
	
	private void doDeactivate() throws SQLException  {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(dropSql())) {
				statement.setString(1, name);
				statement.executeQuery();				
			}
		}
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isMultiConsumer() {
		return multiConsumer;
	}
	@Override
	public String getPayloadType() {
		// TODO Auto-generated method stub
		return payloadType;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public DestinationSpec createDestinationSpec(String name, int retryDelay) {		
		DestinationSpecImpl spec = new DestinationSpecImpl(this, name, retryDelay);
		Bus.getOrmClient().getDestinationSpecFactory().persist(spec);
		spec.activate();
		return spec;
	}
	
	@Override
	public boolean isJms() {
		return payloadType.toUpperCase().startsWith("SYS.AQ$_JMS_");
	}

	public AQQueueTable getAqQueueTable(AQjmsSession session) throws JMSException {
		return session.getQueueTable("kore",name);
	}	
}
