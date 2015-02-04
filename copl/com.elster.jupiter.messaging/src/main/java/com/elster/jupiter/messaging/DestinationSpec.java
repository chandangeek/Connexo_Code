package com.elster.jupiter.messaging;

import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.util.HasName;

import java.util.List;

/**
 * Destination is an abstraction of a topic or queue.
 */
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
     * Create a new subscriber with the given name and worker count.
     *
     * @param name
     * @return
     */
    @TransactionRequired
    SubscriberSpec subscribe(String name);

    SubscriberSpec subscribe(String name, boolean systemManaged);

    void save();
    
    boolean isBuffered();
}
