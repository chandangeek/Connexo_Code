/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageSeeds;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.UnderlyingJmsException;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class DestinationSpecImpl implements DestinationSpec {

    private static class RetryBehavior {
        private final int retries;
        private final Duration retryDelay;

        public RetryBehavior(int retries, Duration retryDelay) {
            this.retries = retries;
            this.retryDelay = retryDelay;
        }

        public int getRetries() {
            return retries;
        }

        public Duration getRetryDelay() {
            return retryDelay;
        }

    }

    // persistent fields
    private String name;
    private String queueTableName;
    private boolean active;
    @Range(min = 0, max = Integer.MAX_VALUE, message = "{" + MessageSeeds.Keys.RETRY_DELAY_OUT_OF_RANGE_KEY + "}")
    private int retryDelay;
    @Range(min = 0, max = Integer.MAX_VALUE, message = "{" + MessageSeeds.Keys.MAX_NUMBER_OF_RETRIES_OUT_OF_RANGE_KEY + "}")
    private int retries;
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

    @Inject
    DestinationSpecImpl(DataModel dataModel, AQFacade aqFacade, Publisher publisher, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.aqFacade = aqFacade;
        this.publisher = publisher;
        this.thesaurus = thesaurus;
    }

    static DestinationSpecImpl from(DataModel dataModel, QueueTableSpec queueTableSpec, String name, int retryDelay, int retries, boolean buffered) {
        return dataModel.getInstance(DestinationSpecImpl.class).init(queueTableSpec, name, retryDelay, retries, buffered);
    }

    @Override
    public QueueTableSpec getQueueTableSpec() {
        if (queueTableSpec == null) {
            queueTableSpec = dataModel.mapper(QueueTableSpec.class).getExisting(queueTableName);
        }
        return queueTableSpec;
    }


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
    public MessageBuilder message(String text) {
        if (getQueueTableSpec().isJms()) {
            throw new UnsupportedOperationException("JMS support not yet implemented.");
        } else {
            return new BytesMessageBuilder(dataModel, aqFacade, publisher, this, text.getBytes());
        }
    }

    @Override
    public MessageBuilder message(byte[] bytes) {
        return new BytesMessageBuilder(dataModel, aqFacade, publisher, this, bytes);
    }

    @Override
    public List<SubscriberSpec> getSubscribers() {
        return ImmutableList.copyOf(subscribers);
    }

    @Override
    public SubscriberSpec subscribe(TranslationKey nameKey, String component, Layer layer) {
        return subscribe(nameKey.getKey(), component, layer, false);
    }

    @Override
    public SubscriberSpec subscribe(TranslationKey nameKey, String component, Layer layer, Condition filter) {
        return subscribe(nameKey.getKey(), component, layer, false, filter);
    }

    @Override
    public SubscriberSpec subscribeSystemManaged(String name) {
        return subscribe(name, null, null, true);
    }

    @Override
    public void save() {
        if (fromDB) {
            Save.UPDATE.save(dataModel, this);
        } else {
            Save.CREATE.save(dataModel, this);
            fromDB = true;
        }

    }

    @Override
    public boolean isBuffered() {
        return buffered;
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

    @Override
    public long numberOfMessages() {
        return getCount(countMessagesSql());
    }

    @Override
    public int numberOfRetries() {
        return retries;
    }

    @Override
    public Duration retryDelay() {
        return Duration.ofSeconds(retryDelay);
    }

    @Override
    public void updateRetryBehavior(int numberOfRetries, Duration retryDelay) {
        this.retryDelay = (int) retryDelay.getSeconds();
        this.retries = numberOfRetries;
        save();
        updateRetryBehavior(new RetryBehavior(numberOfRetries, retryDelay));
    }

    @Override
    public void purgeErrors() {
        doPurgeErrors();
    }

    @Override
    public void purgeCorrelationId(String correlationId) {
        String sql = "DECLARE po dbms_aqadm.aq$_purge_options_t; BEGIN po.block := TRUE; DBMS_AQADM.PURGE_QUEUE_TABLE(queue_table => ?, purge_condition => 'upper(qtview.queue) = upper(''' || ? || ''') and qtview.corr_id = ''' || ? || ''' ', purge_options => po); END;";
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int parameterIndex = 0;
                statement.setString(++parameterIndex, getQueueTableSpec().getName());
                statement.setString(++parameterIndex, name);
                statement.setString(++parameterIndex, correlationId);
                statement.execute();
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public long errorCount() {
        return getCount(countErrorsSql());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "DestinationSpecImpl{" +
                "name='" + name + '\'' +
                '}';
    }

    DestinationSpecImpl init(QueueTableSpec queueTableSpec, String name, int retryDelay, int retries, boolean buffered) {
        this.name = name;
        this.queueTableSpec = queueTableSpec;
        this.queueTableName = queueTableSpec.getName();
        this.retryDelay = retryDelay;
        this.retries = retries;
        this.buffered = buffered;
        this.fromDB = false;
        return this;
    }

    private SubscriberSpec subscribe(String nameKey, String component, Layer layer, boolean systemManaged) {
        return subscribe(nameKey, component, layer, systemManaged, null);
    }

    private SubscriberSpec subscribe(String nameKey, String component, Layer layer, boolean systemManaged, Condition filter) {
        if (!isActive()) {
            throw new InactiveDestinationException(thesaurus, this, nameKey);
        }
        List<SubscriberSpec> currentConsumers = subscribers;
        for (SubscriberSpec each : currentConsumers) {
            if (each.getName().equals(nameKey)) {
                throw new DuplicateSubscriberNameException(thesaurus, nameKey);
            }
        }
        if (isQueue() && !currentConsumers.isEmpty()) {
            throw new AlreadyASubscriberForQueueException(thesaurus, this);
        }
        SubscriberSpecImpl result = SubscriberSpecImpl.from(dataModel, this, nameKey, component, layer, systemManaged, filter);
        result.subscribe();
        subscribers.add(result);
        dataModel.mapper(DestinationSpec.class).update(this);
        return result;
    }

    private void doActivateAq() {
        try (Connection connection = dataModel.getConnection(false)) {
            tryActivate(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void tryActivate(Connection connection) throws SQLException {
        String sql = "BEGIN dbms_aqadm.create_queue(queue_name => ?, queue_table => ?, retry_delay => ?, max_retries => ?); dbms_aqadm.start_queue(?); END;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameterIndex = 0;
            statement.setString(++parameterIndex, name);
            statement.setString(++parameterIndex, queueTableName);
            statement.setInt(++parameterIndex, retryDelay);
            statement.setInt(++parameterIndex, retries);
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

    private long getCount(String countSql) {
        try {
            try (Connection connection = dataModel.getConnection(false)) {
                try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                    statement.setString(1, name.toUpperCase());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getLong(1);
                        } else {
                            return 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private String countMessagesSql() {
        return "select count(*) from AQ$" + getQueueTableSpec().getName() + " where QUEUE = ?";
    }

    private String countErrorsSql() {
        return "select count(*) from AQ$" + getQueueTableSpec().getName() + " where ORIGINAL_QUEUE_NAME = ?";
    }

    private RetryBehavior queryRetryBehavior() {
        try {
            try (Connection connection = dataModel.getConnection(false)) {
                try (PreparedStatement statement = connection.prepareStatement(retryBehaviorSql())) {
                    statement.setString(1, name);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int retries = resultSet.getInt(1);
                            long durationInSeconds = resultSet.getLong(2);
                            return new RetryBehavior(retries, Duration.ofSeconds(durationInSeconds));
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private String retryBehaviorSql() {
        return "select MAX_RETRIES, RETRY_DELAY from user_queues where NAME = ?";
    }

    private void updateRetryBehavior(RetryBehavior retryBehavior) {
        String sql = "BEGIN DBMS_AQADM.ALTER_QUEUE(queue_name => ?, retry_delay => ?, max_retries => ?); END;";
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int parameterIndex = 0;
                statement.setString(++parameterIndex, name);
                statement.setLong(++parameterIndex, retryBehavior.getRetryDelay().getSeconds());
                statement.setInt(++parameterIndex, retryBehavior.getRetries());
                statement.execute();
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void doPurgeErrors() {
        String sql = "DECLARE po dbms_aqadm.aq$_purge_options_t; BEGIN po.block := TRUE; DBMS_AQADM.PURGE_QUEUE_TABLE(queue_table => ?, purge_condition => 'upper(qtview.original_queue_name) = upper(''" + name + "'')', purge_options => po); END;";
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int parameterIndex = 0;
                statement.setString(++parameterIndex, getQueueTableSpec().getName());
                statement.execute();
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
