package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import org.joda.time.Seconds;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class TransientDestinationSpec implements DestinationSpec {

    private final QueueTableSpec queueTableSpec;
    private boolean active;
    private final List<TransientSubscriberSpec> subscribers = new CopyOnWriteArrayList<>();
    private final String name;

    public TransientDestinationSpec(QueueTableSpec queueTableSpec, String name) {
        this.queueTableSpec = queueTableSpec;
        this.name = name;
    }

    @Override
    public QueueTableSpec getQueueTableSpec() {
        return queueTableSpec;
    }

    @Override
    public void activate() {
        this.active = true;

    }

    @Override
    public void deactivate() {
        active = false;

    }

    @Override
    public boolean isTopic() {
        return queueTableSpec.isMultiConsumer();
    }

    @Override
    public boolean isQueue() {
        return !isTopic();
    }

    @Override
    public String getPayloadType() {
        return queueTableSpec.getPayloadType();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public MessageBuilder message(String text) {
        return new TransientMessageBuilder(text);
    }

    @Override
    public MessageBuilder message(byte[] bytes) {
        return new TransientMessageBuilder(bytes);
    }

    @Override
    public List<SubscriberSpec> getSubscribers() {
        return Collections.<SubscriberSpec>unmodifiableList(subscribers);
    }

    @Override
    public SubscriberSpec subscribe(String name) {
        if (!active) {
            throw new InactiveDestinationException(this, name);
        }
        if (!queueTableSpec.isMultiConsumer() && !subscribers.isEmpty()) {
            throw new AlreadyASubscriberForQueueException(this);
        }
        for (TransientSubscriberSpec subscriber : subscribers) {
            if (subscriber.getName().equals(name)) {
                throw new DuplicateSubscriberNameException(name);
            }
        }
        TransientSubscriberSpec subscriberSpec = new TransientSubscriberSpec(this, name);
        subscribers.add(subscriberSpec);
        return subscriberSpec;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void save() {
    }

    private class TransientMessageBuilder implements MessageBuilder {

        private final byte[] data;

        public TransientMessageBuilder(String text) {
            data = text.getBytes();
        }

        public TransientMessageBuilder(byte[] bytes) {
            data = new byte[bytes.length];
            System.arraycopy(bytes, 0, data, 0, bytes.length);
        }

        @Override
        public void send() {
            for (TransientSubscriberSpec subscriber : subscribers) {
                subscriber.addMessage(new TransientMessage(data));
            }

        }

        @Override
        public MessageBuilder expiringAfter(Seconds seconds) {
            return this;
        }
    }
}
