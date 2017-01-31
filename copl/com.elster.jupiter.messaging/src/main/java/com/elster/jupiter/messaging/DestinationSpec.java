/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.util.List;

/**
 * Destination is an abstraction of a topic or queue.
 */
@ProviderType
public interface DestinationSpec extends HasName {

    /**
     * @return the QueueTableSpec in which this Destination is defined.
     */
    QueueTableSpec getQueueTableSpec();

    /**
     * Activates this Destination, from which point messages can be sent to it.
     */
    void activate();

    /**
     * Deactivates this Destination
     */
    void deactivate();

    /**
     * @return true if this is a topic, i.e. allows multiple subscribers, false otherwise
     */
    boolean isTopic();

    /**
     * @return true if this is a queue, i.e. allows just one subscriber, false otherwise
     */
    boolean isQueue();

    /**
     * @return the payload type of the message : RAW or the Oracle data type or the name of the User Defined Type.
     */
    String getPayloadType();

    /**
     * @return true if active, false otherwise
     */
    boolean isActive();

    /**
     * @param text payload
     * @return a MessageBuilder initialized with the given text as payload, whose send() method will send the message to this Destination.
     */
    MessageBuilder message(String text);

    /**
     * @param bytes payload
     * @return a MessageBuilder initialized with the given byte array as payload, whose send() method will send the message to this Destination.
     */
    MessageBuilder message(byte[] bytes);

    /**
     * @return a List containing the current Subscribers
     */
    List<SubscriberSpec> getSubscribers();

    /**
     * Create a new subscriber whose name and display name
     * is determined by the TranslationKey.
     * The key of the TranslationKey is used as the name
     * and the translation is obviously used as display name.
     *
     * @param nameKey The TranslationKey
     * @param component The component that provides a translation for the specified key
     * @param layer The layer that contains the translation for the specified key
     * @return The newly created SubscriberSpec
     */
    @TransactionRequired
    SubscriberSpec subscribe(TranslationKey nameKey, String component, Layer layer);

    SubscriberSpec subscribe(TranslationKey nameKey, String component, Layer layer, Condition filter);

    SubscriberSpec subscribeSystemManaged(String name);

    void save();

    boolean isBuffered();

    /**
     * Unsubscribes the subscriber with the passed name
     *
     * @param subscriberSpecName the name of the subscriber
     * @since 2.0
     */
    @TransactionRequired
    void unSubscribe(String subscriberSpecName);

    /**
     * since 2.0
     */
    void delete();

    long numberOfMessages();

    int numberOfRetries();

    Duration retryDelay();

    void updateRetryBehavior(int numberOfRetries, Duration retryDelay);

    void purgeErrors();

    void purgeCorrelationId(String correlationId);

    long errorCount();

    static Where whereCorrelationId() {
        return Where.where("corrid");
    }

    long getVersion();
}
