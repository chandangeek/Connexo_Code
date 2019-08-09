package com.elster.jupiter.offline.services.messaging.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Duration;
import java.util.List;

public class DefaultDestinationSpecImpl implements DestinationSpec {

    private String name;

    public DefaultDestinationSpecImpl(String name) {
        this.name = name;
    }

    @Override
    public QueueTableSpec getQueueTableSpec() {
        return null;
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public boolean isTopic() {
        return false;
    }

    @Override
    public boolean isQueue() {
        return false;
    }

    @Override
    public boolean isPrioritized(){
        return false;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public String getQueueTypeName() {
        return null;
    }

    @Override
    public boolean isExtraQueueCreationEnabled() {
        return false;
    }

    @Override
    public String getPayloadType() {
        return null;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public MessageBuilder message(String text) {
        return new BytesMessageBuilder(this, text.getBytes());
    }

    @Override
    public MessageBuilder message(byte[] bytes) {
        return null;
    }

    @Override
    public List<SubscriberSpec> getSubscribers() {
        return null;
    }

    @Override
    public SubscriberSpec subscribe(TranslationKey nameKey, String component, Layer layer) {
        return null;
    }

    @Override
    public SubscriberSpec subscribe(TranslationKey nameKey, String component, Layer layer, Condition filter) {
        return null;
    }

    @Override
    public SubscriberSpec subscribeSystemManaged(String name) {
        return null;
    }

    @Override
    public void save() {

    }

    @Override
    public boolean isBuffered() {
        return false;
    }

    @Override
    public void unSubscribe(String subscriberSpecName) {

    }

    @Override
    public void delete() {

    }

    @Override
    public long numberOfMessages() {
        return 0;
    }

    @Override
    public int numberOfRetries() {
        return 0;
    }

    @Override
    public Duration retryDelay() {
        return null;
    }

    @Override
    public void updateRetryBehavior(int numberOfRetries, Duration retryDelay) {

    }

    @Override
    public void purgeErrors() {

    }

    @Override
    public void purgeCorrelationId(String correlationId) {

    }

    @Override
    public long errorCount() {
        return 0;
    }

    @Override
    public long getVersion() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }
}
