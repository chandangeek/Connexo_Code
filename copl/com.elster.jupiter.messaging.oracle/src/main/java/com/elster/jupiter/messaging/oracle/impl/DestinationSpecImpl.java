package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.UnderlyingJmsException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.conditions.Condition;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class DestinationSpecImpl implements DestinationSpec {

    // persistent fields
    private String name;
    private String queueTableName;
    private boolean active;
    private int retryDelay;
    private boolean buffered;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    // associations
    private QueueTableSpec queueTableSpec;
    private final List<SubscriberSpec> subscribers = new ArrayList<>();
    private final DataModel dataModel;
    private final AQFacade aqFacade;
    private final Publisher publisher;
    private final Thesaurus thesaurus;
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
        return subscribe(name, false);
    }

    @Override
    public SubscriberSpec subscribe(String name, Condition filter) {
        return subscribe(name, false, filter);
    }

    @Override
    public SubscriberSpec subscribeSystemManaged(String name) {
        return subscribe(name, true);
    }

    private SubscriberSpec subscribe(String name, boolean systemManaged) {
        return subscribe(name, systemManaged, null);
    }

    private SubscriberSpec subscribe(String name, boolean systemManaged, Condition filter) {
        if (!isActive()) {
            throw new InactiveDestinationException(thesaurus, this, name);
        }
        List<SubscriberSpec> currentConsumers = subscribers;
        for (SubscriberSpec each : currentConsumers) {
            if (each.getName().equals(name)) {
                throw new DuplicateSubscriberNameException(thesaurus, name);
            }
        }
        if (isQueue() && !currentConsumers.isEmpty()) {
            throw new AlreadyASubscriberForQueueException(thesaurus, this);
        }
        SubscriberSpecImpl result = SubscriberSpecImpl.from(dataModel, this, name, systemManaged, filter);
        result.subscribe();
        subscribers.add(result);
        dataModel.mapper(DestinationSpec.class).update(this);
        return result;
    }

    @Override
    public void unSubscribe(String subscriberSpecName) {
        if (!isActive()) {
            throw new InactiveDestinationException(thesaurus, this, name);
        }
        List<SubscriberSpec> currentConsumers = subscribers;
        Optional<SubscriberSpec> subscriberSpecRef = currentConsumers.stream().filter(ss -> ss.getName().equals(subscriberSpecName)).findFirst();
        if (subscriberSpecRef.isPresent()) {
            SubscriberSpecImpl subscriberSpec = SubscriberSpecImpl.class.cast(subscriberSpecRef.get());
            subscriberSpec.unSubscribe();
            subscribers.remove(subscriberSpec);
            dataModel.mapper(DestinationSpec.class).update(this);
        }
    }

    @Override
    public void delete() {
        if (isActive()) {
            deactivate();
        }
        dataModel.mapper(DestinationSpec.class).remove(this);
    }

    DestinationSpecImpl init(QueueTableSpec queueTableSpec, String name, int retryDelay,boolean buffered) {
        this.name = name;
        this.queueTableSpec = queueTableSpec;
        this.queueTableName = queueTableSpec.getName();
        this.retryDelay = retryDelay;
        this.buffered = buffered;
        this.fromDB = false;
        return this;
    }

    @Inject
    DestinationSpecImpl(DataModel dataModel, AQFacade aqFacade, Publisher publisher, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.aqFacade = aqFacade;
        this.publisher = publisher;
        this.thesaurus = thesaurus;
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

    static DestinationSpecImpl from(DataModel dataModel, QueueTableSpec queueTableSpec, String name, int retryDelay, boolean buffered) {
        return dataModel.getInstance(DestinationSpecImpl.class).init(queueTableSpec, name, retryDelay,buffered);
    }

    private void doActivateAq() {
        try (Connection connection = dataModel.getConnection(false)) {
            tryActivate(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void tryActivate(Connection connection) throws SQLException {
        String sql = "BEGIN dbms_aqadm.create_queue(queue_name => ?, queue_table => ?, retry_delay => ?); dbms_aqadm.start_queue(?); END;";
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
            throw new UnderlyingJmsException(thesaurus, e);
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

    @Override
    public String toString() {
        return "DestinationSpecImpl{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean isBuffered() {
        return buffered;
    }
}
