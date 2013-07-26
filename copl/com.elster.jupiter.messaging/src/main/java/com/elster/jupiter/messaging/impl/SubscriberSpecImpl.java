package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.time.UtcInstant;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class SubscriberSpecImpl implements SubscriberSpec {
	private String name;
	private String destinationName;

	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime; 
	@SuppressWarnings("unused")
	private String userName;
	
	private DestinationSpec destination;

    private volatile OracleConnection cancellableConnection;

    private Object cancelLock = new Object();
	
	@SuppressWarnings("unused")
	private SubscriberSpecImpl() {
	}
	
	SubscriberSpecImpl(DestinationSpec destination, String name) {
		this.destination = destination;
		this.destinationName = destination.getName();
		this.name = name;
	}
	
	@Override
	public DestinationSpec getDestination() {
		if (destination == null) {
			destination = Bus.getOrmClient().getDestinationSpecFactory().getExisting(destinationName);
		}
		return destination;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
    public AQMessage receive() throws SQLException {
		try (Connection connection = Bus.getConnection()) {
			cancellableConnection= connection.unwrap(OracleConnection.class);
            return cancellableConnection.dequeue(destinationName, basicOptions(), getDestination().getPayloadType());
        } catch (SQLTimeoutException e) {
            // we don't specify a timeout, so this means the connection got a cancel() call, requesting we're shutting down, so we will
            // no recovery needed, ignoring exception.
		} finally {
            cancellableConnection = null;
        }
        return null;
    }

    @Override
    public void cancel() throws SQLException {
        synchronized (cancelLock) {
            if (cancellableConnection != null) {
                cancellableConnection.cancel();
            }
        }
    }

    private AQDequeueOptions basicOptions() throws SQLException {
        AQDequeueOptions options = new AQDequeueOptions();
        if (getDestination().isTopic()) {
            options.setConsumerName(name);
        }
        return options;
    }

    AQMessage receiveNow() throws SQLException {
        try (Connection connection = Bus.getConnection()) {
            OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
            return oraConnection.dequeue(destinationName, optionsNoWait(), getDestination().getPayloadType());
        }
    }

    private AQDequeueOptions optionsNoWait() throws SQLException {
        AQDequeueOptions options = basicOptions();
        options.setWait(0);
        return options;
    }

    void subscribe() {
		if (getDestination().isQueue()) {
			return;
		}
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
			"begin subscriber := sys.aq$_agent(?,null,null); " +
			"dbms_aqadm.add_subscriber(?,subscriber); end;";
		
				
	}
	
	private String unSubscribeSql() {
		return 
			"declare subscriber sys.aq$_agent;  " +
			"begin subscriber := sys.aq$_agent(?,null,null); + " +
			"dbms_aqadm.remove_subscriber(?,subscriber); end;";		
	}
	
}

