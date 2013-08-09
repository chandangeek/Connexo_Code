package com.elster.jupiter.messaging;

import com.elster.jupiter.orm.TransactionRequired;

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

    List<SubscriberSpec> getConsumers();

    @TransactionRequired
    SubscriberSpec subscribe(String name, int workerCount);
}
