package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQMessage;
import org.joda.time.Seconds;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/**
 * SubscriberSpec implementation.
 */
public class SubscriberSpecImpl implements SubscriberSpec {

    /**
     * Receive will wait this long before checking whether to continue or not.
     */
    private static final Seconds DEFAULT_WAIT = Seconds.seconds(60);
    private String name;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    @SuppressWarnings("unused")
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final Reference<DestinationSpec> destination = ValueReference.absent();

    private volatile OracleConnection cancellableConnection;

    private final Object cancelLock = new Object();

    private final DataModel dataModel;

    @Inject
    SubscriberSpecImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    SubscriberSpecImpl init(DestinationSpec destination, String name) {
        this.destination.set(destination);
        this.name = name;
        return this;
    }

    static SubscriberSpecImpl from(DataModel dataModel, DestinationSpec destinationSpec, String name) {
        return dataModel.getInstance(SubscriberSpecImpl.class).init(destinationSpec, name);
    }

    @Override
    public DestinationSpec getDestination() {
        return destination.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Message receive() {
        try {
            return tryReceive();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Message tryReceive() throws SQLException {
        try (Connection connection = getConnection()) {
            cancellableConnection = connection.unwrap(OracleConnection.class);
            AQMessage aqMessage = null;
            try {
                while (aqMessage == null) {
                    aqMessage = dequeueMessage();
                }
                return new MessageImpl(aqMessage);
            } catch (SQLTimeoutException e) {
                /*
                      The connection has been canceled.
                      We'll ignore this exception, since we have a way to recover from it (i.e. stop waiting as requested).
                 */
            }
        } finally {
            cancellableConnection = null;
        }
        return null;
    }

    private Connection getConnection() throws SQLException {
        return dataModel.getConnection(false);
    }

    private AQMessage dequeueMessage() throws SQLException {
        return cancellableConnection.dequeue(destination.get().getName(), basicOptions(), getDestination().getPayloadType());
    }

    @Override
    public void cancel() {
        synchronized (cancelLock) {
            OracleConnection localCopy = cancellableConnection;
            if (localCopy != null) {
                try {
                    localCopy.cancel();
                } catch (SQLException e) {
                    throw new UnderlyingSQLFailedException(e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "SubscriberSpecImpl{" +
                "name='" + name + '\'' +
                ", destinationName='" + destination.get().getName() + '\'' +
                '}';
    }

    private AQDequeueOptions basicOptions() throws SQLException {
        AQDequeueOptions options = new AQDequeueOptions();
        options.setWait(DEFAULT_WAIT.getSeconds());
        if (getDestination().isTopic()) {
            options.setConsumerName(name);
            options.setNavigation(AQDequeueOptions.NavigationOption.FIRST_MESSAGE);
        }
        return options;
    }

    /**
     * @return the message if one is immediately available, null otherwise
     */
    AQMessage receiveNow() {
        try (Connection connection = getConnection()) {
            OracleConnection oraConnection = connection.unwrap(OracleConnection.class);
            return oraConnection.dequeue(destination.get().getName(), optionsNoWait(), getDestination().getPayloadType());
        } catch (SQLTimeoutException e) {
            return null;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
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
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    void doSubscribe() throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(subscribeSql())) {
                statement.setString(1, name);
                statement.setString(2, destination.get().getName());
                statement.execute();
            }
        }
    }

    public void unSubscribe() {
        try {
            doUnSubscribe(name);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    void doUnSubscribe(String subscriberName) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(unSubscribeSql())) {
                statement.setString(1, subscriberName);
                statement.setString(2, destination.get().getName());
                statement.execute();
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

