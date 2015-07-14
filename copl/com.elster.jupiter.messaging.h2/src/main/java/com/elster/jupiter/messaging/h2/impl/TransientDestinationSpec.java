package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

class TransientDestinationSpec implements DestinationSpec {

    private final QueueTableSpec queueTableSpec;
    private boolean active;
    private final List<TransientSubscriberSpec> subscribers = new CopyOnWriteArrayList<>();
    private final String name;
    private final Thesaurus thesaurus;
    private final boolean buffered;

    public TransientDestinationSpec(QueueTableSpec queueTableSpec, Thesaurus thesaurus, String name, boolean buffered) {
        this.queueTableSpec = queueTableSpec;
        this.thesaurus = thesaurus;
        this.name = name;
        this.buffered = buffered;
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
            throw new InactiveDestinationException(thesaurus, this, name);
        }
        if (!queueTableSpec.isMultiConsumer() && !subscribers.isEmpty()) {
            throw new AlreadyASubscriberForQueueException(thesaurus, this);
        }
        for (TransientSubscriberSpec subscriber : subscribers) {
            if (subscriber.getName().equals(name)) {
                throw new DuplicateSubscriberNameException(thesaurus, name);
            }
        }
        TransientSubscriberSpec subscriberSpec = new TransientSubscriberSpec(this, name);
        subscribers.add(subscriberSpec);
        return subscriberSpec;
    }

    @Override
    public SubscriberSpec subscribe(String name, Condition filter) {
        return subscribe(name);
    }

    @Override
    public void unSubscribe(String subscriberSpecName) {
        if (!isActive()) {
            throw new InactiveDestinationException(thesaurus, this, name);
        }
        List<TransientSubscriberSpec> currentConsumers = subscribers;
        Optional<TransientSubscriberSpec> subscriberSpecRef = currentConsumers.stream().filter(ss -> ss.getName().equals(subscriberSpecName)).findFirst();
        if (subscriberSpecRef.isPresent()) {
            subscribers.remove(subscriberSpecRef.get());
        }
    }

    @Override
    public void delete() {
        if (isActive()) {
            deactivate();
        }
    }

    @Override
    public SubscriberSpec subscribeSystemManaged(String name) {
        return subscribe(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void save() {
    }
    
    @Override
    public boolean isBuffered() {
    	return buffered;
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
        public MessageBuilder expiringAfter(Duration duration) {
            return this;
        }

        @Override
        public MessageBuilder withCorrelationId(String correlationId) {
            return this;
        }
    }
}
