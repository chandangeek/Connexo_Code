/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.messaging.impl;

import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.messaging.*;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class OfflineDestinationSpecImpl implements DestinationSpec {

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
    private boolean isDefault;
    private String queueTypeName;
    private boolean isExtraQueueCreationEnabled;
    private boolean prioritized;

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
    private final Publisher publisher;
    private final Thesaurus thesaurus;
    private boolean fromDB = true;

    @Inject
    OfflineDestinationSpecImpl(DataModel dataModel, Publisher publisher, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.publisher = publisher;
        this.thesaurus = thesaurus;
    }

    static OfflineDestinationSpecImpl from(DataModel dataModel, QueueTableSpec queueTableSpec, String name, int retryDelay, int retries, boolean buffered, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled) {
        return dataModel.getInstance(OfflineDestinationSpecImpl.class).init(queueTableSpec, name, retryDelay, retries, buffered, isDefault, queueTypeName, isExtraQueueCreationEnabled, false);
    }

    static OfflineDestinationSpecImpl from(DataModel dataModel, QueueTableSpec queueTableSpec, String name, int retryDelay, int retries, boolean buffered, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled, boolean prioritized) {
        return dataModel.getInstance(OfflineDestinationSpecImpl.class).init(queueTableSpec, name, retryDelay, retries, buffered, isDefault, queueTypeName, isExtraQueueCreationEnabled, prioritized);
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
        active = true;
        dataModel.mapper(DestinationSpec.class).update(this, "active");
    }

    @Override
    public void deactivate() {
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
            return new BytesMessageBuilder(this, text.getBytes());
        }
    }

    @Override
    public MessageBuilder message(byte[] bytes) {
        return new BytesMessageBuilder(this, bytes);
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
            SubscriberSpec subscriberSpec = OfflineSubscriberSpecImpl.class.cast(subscriberSpecRef.get());
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
        return 0;
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
    }

    @Override
    public void purgeErrors() {

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
        return 0;
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
        return "OfflineDestinationSpecImpl{" +
                "name='" + name + '\'' +
                '}';
    }

    OfflineDestinationSpecImpl init(QueueTableSpec queueTableSpec, String name, int retryDelay, int retries, boolean buffered, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled, boolean prioritized) {
        this.name = name;
        this.queueTableSpec = queueTableSpec;
        this.queueTableName = queueTableSpec.getName();
        this.retryDelay = retryDelay;
        this.retries = retries;
        this.buffered = buffered;
        this.isDefault = isDefault;
        this.queueTypeName = queueTypeName;
        this.isExtraQueueCreationEnabled = isExtraQueueCreationEnabled;
        this.prioritized = prioritized;
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
        SubscriberSpec result = OfflineSubscriberSpecImpl.from(dataModel, this, nameKey, component, layer, systemManaged, filter);
//        result.subscribe();
        subscribers.add(result);
        dataModel.mapper(DestinationSpec.class).update(this);
        return result;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String getQueueTypeName() {
        return queueTypeName;
    }

    @Override
    public boolean isExtraQueueCreationEnabled() {
        return isExtraQueueCreationEnabled;
    }

    @Override
    public boolean isPrioritized() {
        return prioritized;
    }
}
