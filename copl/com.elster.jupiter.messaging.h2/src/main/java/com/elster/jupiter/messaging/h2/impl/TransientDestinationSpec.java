/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.elster.jupiter.util.streams.Currying.perform;

class TransientDestinationSpec implements DestinationSpec {

    private final QueueTableSpec queueTableSpec;
    private boolean active;
    private final List<TransientSubscriberSpec> subscribers = new CopyOnWriteArrayList<>();
    private final String name;
    private final Thesaurus thesaurus;
    private final boolean buffered;

    TransientDestinationSpec(QueueTableSpec queueTableSpec, Thesaurus thesaurus, String name, boolean buffered) {
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
        return Collections.unmodifiableList(subscribers);
    }

    public SubscriberSpec subscribe(String name, String displayName) {
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
        TransientSubscriberSpec subscriberSpec = new TransientSubscriberSpec(this, name, displayName);
        subscribers.add(subscriberSpec);
        return subscriberSpec;
    }

    @Override
    public SubscriberSpec subscribe(TranslationKey nameKey, String component, Layer layer) {
        return this.subscribe(nameKey.getKey(), nameKey.getDefaultFormat());
    }

    @Override
    public SubscriberSpec subscribe(TranslationKey nameKey, String component, Layer layer, Condition filter) {
        return this.subscribe(nameKey, component, layer);
    }

    @Override
    public void unSubscribe(String subscriberSpecName) {
        if (!isActive()) {
            throw new InactiveDestinationException(thesaurus, this, name);
        }
        Optional<TransientSubscriberSpec> subscriberSpecRef = subscribers.stream().filter(ss -> ss.getName().equals(subscriberSpecName)).findFirst();
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
        return subscribe(name, name);
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

    @Override
    public long numberOfMessages() {
        return subscribers.stream()
                .mapToLong(TransientSubscriberSpec::messageCount)
                .sum();
    }

    @Override
    public int numberOfRetries() {
        return 0;
    }

    @Override
    public Duration retryDelay() {
        return Duration.of(0, ChronoUnit.SECONDS);
    }

    @Override
    public void updateRetryBehavior(int numberOfRetries, Duration retryDelay) {
    }

    @Override
    public void purgeErrors() {
    }

    @Override
    public void purgeCorrelationId(String correlationId) {
       subscribers.forEach(perform(TransientSubscriberSpec::removeMessagesWithCorrelationId).with(correlationId));
    }

    @Override
    public long errorCount() {
        return 0;
    }

    @Override
    public long getVersion() {
        return 0;
    }

    private class TransientMessageBuilder implements MessageBuilder {

        private final byte[] data;
        private String correlationId;

        TransientMessageBuilder(String text) {
            data = text.getBytes();
        }

        TransientMessageBuilder(byte[] bytes) {
            data = new byte[bytes.length];
            System.arraycopy(bytes, 0, data, 0, bytes.length);
        }

        @Override
        public void send() {
            for (TransientSubscriberSpec subscriber : subscribers) {
                TransientMessage message = new TransientMessage(data);
                message.setCorrelationId(correlationId);
                subscriber.addMessage(message);
            }

        }

        @Override
        public MessageBuilder expiringAfter(Duration duration) {
            return this;
        }

        @Override
        public MessageBuilder withCorrelationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
    }
}
