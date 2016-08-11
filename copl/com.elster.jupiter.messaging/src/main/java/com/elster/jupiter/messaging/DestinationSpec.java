package com.elster.jupiter.messaging;

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

    long getVersion();

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
     * Starts the creation of a new subscriber with the given name.
     *
     * @param name The name of the subscriber that will be built
     * @return The SubscriberSpecBuilder
     * @since 2.0
     */
    @TransactionRequired
    SubscriberSpecBuilder subscribe(String name);

    boolean isBuffered();

    /**
     * Unsubscribes the subscriber with the passed name
     *
     * @param subscriberSpecName the name of the subscriber
     * @since 1.1
     */
    @TransactionRequired
    void unSubscribe(String subscriberSpecName);

    long numberOfMessages();

    int numberOfRetries();

    long errorCount();

    Duration retryDelay();

    void updateRetryBehavior(int numberOfRetries, Duration retryDelay);

    void purgeErrors();

    void purgeCorrelationId(String correlationId);

    static Where whereCorrelationId() {
        return Where.where("corrid");
    }

    void save();

    /**
     * @since 1.1
     */
    void delete();

    @ProviderType
    interface SubscriberSpecBuilder {
        SubscriberSpecBuilder with(Condition filter);
        SubscriberSpecBuilder with(DequeueOptions dequeueOptions);
        default SubscriberSpecBuilder systemManaged() {
            return this.systemManaged(true);
        }
        SubscriberSpecBuilder systemManaged(boolean flag);
        SubscriberSpec create();
    }

}