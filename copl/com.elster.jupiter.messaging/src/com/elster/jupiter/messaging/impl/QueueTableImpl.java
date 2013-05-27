package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;

import com.elster.jupiter.messaging.QueueTable;
import com.elster.jupiter.util.time.UtcInstant;

import oracle.AQ.AQException;
import oracle.AQ.AQQueueTableProperty;
import oracle.jms.AQjmsQueueConnectionFactory;
import oracle.jms.AQjmsSession;

public class QueueTableImpl implements QueueTable {	
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
	private QueueTableImpl() {		
	}
	public QueueTableImpl(String name , String payloadType , boolean multiConsumer) {
		this.name = name;
		this.payloadType = payloadType;
		this.multiConsumer = multiConsumer;	
	}
	
	@Override
	public void activate() {
		try {
			doActivate();
		} catch (SQLException | JMSException | AQException ex) {
			throw new RuntimeException(ex);
		}
		active = true;
		Bus.getOrmClient().getQueueTableFactory().update(this,"active");
	}
	
	private String getDatabaseUser(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("select user from dual")) {
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getString(1);
			}
		}
	}
	
	private void doActivate() throws SQLException, JMSException, AQException {
		try (Connection connection = Bus.getConnection()) {
			AQjmsSession session = getSession(connection);
			AQQueueTableProperty properties = new AQQueueTableProperty(payloadType);
			properties.setMultiConsumer(multiConsumer);			
			session.createQueueTable(getDatabaseUser(connection),name,properties);			
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
		Bus.getOrmClient().getQueueTableFactory().update(this,"active");
	}
	
	private String dropSql() {
		return "begin dbms_aqadm.drop_queue_table(?); end;";
		
				
	}
	
	private void doDeActivate() throws SQLException  {
		try (Connection connection = Bus.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(dropSql())) {
				statement.setString(1, name);
				statement.executeQuery();				
			}
		}
	}
	
	private AQjmsSession getSession(Connection connection) throws JMSException {
		QueueConnection queueConnection = AQjmsQueueConnectionFactory.createQueueConnection(connection);
		queueConnection.start();
		return (AQjmsSession) queueConnection.createSession(true, Session.AUTO_ACKNOWLEDGE);		
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
	
}
