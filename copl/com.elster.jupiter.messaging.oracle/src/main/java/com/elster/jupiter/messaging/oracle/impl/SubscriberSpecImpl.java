package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQMessage;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * SubscriberSpec implementation.
 */
class SubscriberSpecImpl implements SubscriberSpec {

    /**
     * Default duration that we are willing to wait for a messages while dequeueing.
     */
    private static final Duration DEQUEUE_DEFAULT_WAIT = Duration.ofSeconds(10);

    /**
     * Default duration that we are willing to wait between dequeueing when no messages were available.
     */
    private static final Duration INBETWEEN_DEQUEUE_DEFAULT_WAIT = Duration.ofSeconds(20);

    private String name;
    @Size(max = 4000)
    private String filter;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private boolean systemManaged;

    private final Reference<DestinationSpec> destination = ValueReference.absent();

    private final Collection<OracleConnection> cancellableConnections = Collections.synchronizedSet(new HashSet<OracleConnection>());

    private final DataModel dataModel;

    @Inject
    SubscriberSpecImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private SubscriberSpecImpl init(DestinationSpec destination, String name) {
        return this.init(destination, name, false);
    }

    private SubscriberSpecImpl init(DestinationSpec destination, String name, boolean systemManaged) {
        return init(destination, name, systemManaged, null);
    }

    private SubscriberSpecImpl init(DestinationSpec destination, String name, boolean systemManaged, Condition filter) {
        this.destination.set(destination);
        this.name = name;
        this.systemManaged = systemManaged;
        this.filter = toString(filter);
        return this;
    }

    private String toString(Condition filter) {
        if (filter != null) {
            ConditionVisitor visitor = new ConditionVisitor();
            filter.visit(visitor);
            return visitor.toString();
        }
        return null;
    }

    static SubscriberSpecImpl from(DataModel dataModel, DestinationSpec destinationSpec, String name) {
        return dataModel.getInstance(SubscriberSpecImpl.class).init(destinationSpec, name);
    }

    static SubscriberSpecImpl from(DataModel dataModel, DestinationSpec destinationSpec, String name, boolean systemManaged, Condition filter) {
        return dataModel.getInstance(SubscriberSpecImpl.class).init(destinationSpec, name, systemManaged, filter);
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

    @Override
    public boolean isSystemManaged() {
        return systemManaged;
    }

    private Message tryReceive() throws SQLException {
        OracleConnection cancellableConnection = null;
        Message message = null;
        NullValueCause nullValueCause = NullValueCause.INITIAL;
        while (message == null && nullValueCause != NullValueCause.CONNECTION_CANCELLED) {
            try (Connection connection = getConnection()) {
                cancellableConnection = connection.unwrap(OracleConnection.class);
                cancellableConnections.add(cancellableConnection);
                ReceiveAttempt attempt = new ReceiveAttempt();
                attempt.tryReceive(cancellableConnection);
                message = attempt.message;
                nullValueCause = attempt.nullValueCause;
            } finally {
                if (cancellableConnection != null) {
                    cancellableConnections.remove(cancellableConnection);
                }
            }
            if (nullValueCause == NullValueCause.DEQUEUE_TIMED_OUT) {
                this.sleep();
            }
        }
        return message;
    }

    private void sleep() {
        try {
            Thread.sleep(INBETWEEN_DEQUEUE_DEFAULT_WAIT.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Connection getConnection() throws SQLException {
        return dataModel.getConnection(false);
    }

    @Override
    public void cancel() {
        synchronized (cancellableConnections) {
            for (OracleConnection cancellableConnection : new ArrayList<>(cancellableConnections)) {
                try {
                    cancellableConnection.cancel();
                    cancellableConnections.remove(cancellableConnection);
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
        options.setWait((int) DEQUEUE_DEFAULT_WAIT.getSeconds());
        if (getDestination().isTopic()) {
            options.setConsumerName(name);
            options.setNavigation(AQDequeueOptions.NavigationOption.FIRST_MESSAGE);
        }
        if (getDestination().isBuffered()) {
            options.setVisibility(AQDequeueOptions.VisibilityOption.IMMEDIATE);
            options.setDeliveryFilter(AQDequeueOptions.DeliveryFilter.BUFFERED);
        }
        return options;
    }

    /**
     * @return the message if one is immediately available, null otherwise
     */
    AQMessage receiveNow() {
        try (Connection connection = getConnection()) {
            return this.dequeue(connection.unwrap(OracleConnection.class), optionsNoWait());
        } catch (SQLTimeoutException e) {
            return null;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private AQMessage dequeue(OracleConnection oracleConnection, AQDequeueOptions options) throws SQLException {
        DestinationSpec destination = getDestination();
        return oracleConnection.dequeue(destination.getName(), options, destination.getPayloadType());
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

    private void doSubscribe() throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(subscribeSql())) {
                statement.setString(1, name);
                statement.setString(2, destination.get().getName());
                statement.setString(3, Checks.is(filter).emptyOrOnlyWhiteSpace() ? null : filter);
                statement.execute();
            }
        }
    }

    void unSubscribe() {
        try {
            doUnSubscribe(name);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private void doUnSubscribe(String subscriberName) throws SQLException {
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
                        "dbms_aqadm.add_subscriber(?,subscriber, ?); end;";


    }

    private String unSubscribeSql() {
        return
                "declare subscriber sys.aq$_agent;  " +
                        "begin subscriber := sys.aq$_agent(?,null,null); " +
                        "dbms_aqadm.remove_subscriber(?,subscriber); end;";
    }

    private enum NullValueCause {
        /**
         * Null is the initial value when no attempts to retrieve a message has been executed yet.
         */
        INITIAL,
        /**
         * Indicates that the sql connection was canceled
         * as a result of shutting down the app server.
         */
        CONNECTION_CANCELLED,
        /**
         * Indicates that the dequeueMessage method
         * on the oracle connection returned null
         * because there was no message available
         * within the requested time period.
         */
        DEQUEUE_TIMED_OUT;
    }

    private class ReceiveAttempt {
        Message message;
        NullValueCause nullValueCause;
        private void tryReceive(OracleConnection connection) throws SQLException {
            try {
                AQMessage aqMessage = this.dequeueMessage(connection);
                if (aqMessage != null) {
                    this.message = new MessageImpl(aqMessage);
                } else {
                    this.nullValueCause = NullValueCause.DEQUEUE_TIMED_OUT;
                }
            } catch (SQLTimeoutException e) {
                /* The connection has been canceled.
                 * We'll ignore this exception, since we have a way to recover from it (i.e. stop waiting as requested). */
                this.nullValueCause = NullValueCause.CONNECTION_CANCELLED;
            }
        }

        private AQMessage dequeueMessage(OracleConnection cancellableConnection) throws SQLException {
            return dequeue(cancellableConnection, basicOptions());
        }

    }

}