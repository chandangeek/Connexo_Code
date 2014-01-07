package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.UnderlyingJmsException;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

import oracle.AQ.AQQueueTable;
import oracle.jdbc.OracleConnection;
import oracle.jms.AQjmsDestination;
import oracle.jms.AQjmsDestinationProperty;
import oracle.jms.AQjmsSession;

import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class DestinationSpecImpl implements DestinationSpec {

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
    private final List<SubscriberSpec> subscribers = new ArrayList<>();
    private final DataModel dataModel;
    private final AQFacade aqFacade;
    private final Publisher publisher;
    private boolean fromDB = true;


    @Override
    public void activate() {
        if (getQueueTableSpec().isJms()) {
            doActivateJms();
        } else {
            doActivateAq();
        }
        active = true;
        dataModel.mapper(DestinationSpec.class).update(this, "active");
    }

    @Override
    public void deactivate() {
        try {
            doDeactivate();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        active = false;
        dataModel.mapper(DestinationSpec.class).update(this, "active");
    }

    @Override
    public List<SubscriberSpec> getSubscribers() {
        return ImmutableList.copyOf(subscribers);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPayloadType() {
        return getQueueTableSpec().getPayloadType();
    }

    @Override
    public QueueTableSpec getQueueTableSpec() {
        if (queueTableSpec == null) {
            queueTableSpec = dataModel.mapper(QueueTableSpec.class).getExisting(queueTableName);
        }
        return queueTableSpec;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isQueue() {
        return !getQueueTableSpec().isMultiConsumer();
    }

    @Override
    public boolean isTopic() {
        return getQueueTableSpec().isMultiConsumer();
    }

    @Override
    public MessageBuilder message(byte[] bytes) {
        return new BytesMessageBuilder(dataModel, aqFacade, publisher, this, bytes);
    }

    @Override
    public MessageBuilder message(String text) {
        if (getQueueTableSpec().isJms()) {
            throw new UnsupportedOperationException("JMS support not yet implemented.");
        } else {
            return new BytesMessageBuilder(dataModel, aqFacade, publisher, this, text.getBytes());
        }
    }

    @Override
    public SubscriberSpec subscribe(String name) {
        if (!isActive()) {
            throw new InactiveDestinationException(this, name);
        }
        List<SubscriberSpec> currentConsumers = subscribers;
        for (SubscriberSpec each : currentConsumers) {
            if (each.getName().equals(name)) {
                throw new DuplicateSubscriberNameException(name);
            }
        }
        if (isQueue() && !currentConsumers.isEmpty()) {
            throw new AlreadyASubscriberForQueueException(this);
        }
        SubscriberSpecImpl result = SubscriberSpecImpl.from(dataModel, this, name);
        result.subscribe();
        subscribers.add(result);
        dataModel.mapper(DestinationSpec.class).update(this);
        return result;
    }

    DestinationSpecImpl init(QueueTableSpec queueTableSpec, String name, int retryDelay) {
        this.name = name;
        this.queueTableSpec = queueTableSpec;
        this.queueTableName = queueTableSpec.getName();
        this.retryDelay = retryDelay;
        this.fromDB = false;
        return this;
    }

    @Inject
    DestinationSpecImpl(DataModel dataModel, AQFacade aqFacade, Publisher publisher) {
        this.dataModel = dataModel;
        this.aqFacade = aqFacade;
        this.publisher = publisher;
    }

    @Override
    public void save() {
        if (fromDB) {
            dataModel.mapper(DestinationSpec.class).update(this);
        } else {
            dataModel.mapper(DestinationSpec.class).persist(this);
            fromDB = true;
        }

    }

    static DestinationSpecImpl from(DataModel dataModel, QueueTableSpec queueTableSpec, String name, int retryDelay) {
        return dataModel.getInstance(DestinationSpecImpl.class).init(queueTableSpec, name, retryDelay);
    }

    private void doActivateAq() {
        try (Connection connection = dataModel.getConnection(false)) {
            tryActivate(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void tryActivate(Connection connection) throws SQLException {
        String sql = "begin dbms_aqadm.create_queue(queue_name => ?, queue_table => ?, retry_delay => ?); dbms_aqadm.start_queue(?); end;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameterIndex = 0;
            statement.setString(++parameterIndex, name);
            statement.setString(++parameterIndex, queueTableName);
            statement.setInt(++parameterIndex, retryDelay);
            statement.setString(++parameterIndex, name);
            statement.execute();
        }
    }

    private void doActivateJms() {
        try (Connection connection = dataModel.getConnection(false)) {
            tryActivateJms(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        } catch (JMSException e) {
            throw new UnderlyingJmsException(e);
        }
    }

    private void tryActivateJms(Connection connection) throws SQLException, JMSException {
        if (connection.isWrapperFor(OracleConnection.class)) {
            OracleConnection oraConnection = connection.unwrap(OracleConnection.class);
            QueueConnection queueConnection = aqFacade.createQueueConnection(oraConnection);
            try {
                queueConnection.start();
                AQjmsSession session = (AQjmsSession) queueConnection.createSession(true, Session.AUTO_ACKNOWLEDGE);
                AQQueueTable aqQueueTable = ((QueueTableSpecImpl) getQueueTableSpec()).getAqQueueTable(session);
                AQjmsDestinationProperty props = new AQjmsDestinationProperty();
                props.setRetryInterval(retryDelay);
                Destination destination = getQueueTableSpec().isMultiConsumer() ?
                        session.createTopic(aqQueueTable, name, props) :
                        session.createQueue(aqQueueTable, name, props);
                ((AQjmsDestination) destination).start(session, true, true);
            } finally {
                queueConnection.close();
            }
        }
    }

    private void doDeactivate() throws SQLException {
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement statement = connection.prepareStatement(dropSql())) {
                statement.setString(1, name);
                statement.setString(2, name);
                statement.execute();
            }
        }
    }

    private String dropSql() {
        return "begin dbms_aqadm.stop_queue(?); dbms_aqadm.drop_queue(?); end;";
    }

}
