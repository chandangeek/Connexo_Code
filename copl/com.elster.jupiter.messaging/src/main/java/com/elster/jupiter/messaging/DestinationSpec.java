package com.elster.jupiter.messaging;

import oracle.jdbc.aq.AQMessage;

import java.sql.SQLException;
import java.util.List;

public interface DestinationSpec {

    QueueTableSpec getQueueTableSpec();

    void activate();

    void deactivate();

    String getName();

    boolean isTopic();

    boolean isQueue();

    String getPayloadType();

    boolean isActive();

    MessageBuilder message(String text);

    MessageBuilder message(byte[] bytes);

    MessageBuilder message(AQMessage message) throws SQLException;

    List<SubscriberSpec> getConsumers();

    SubscriberSpec subscribe(String name, int workerCount);
}
