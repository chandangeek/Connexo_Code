package com.elster.jupiter.pubsub;

/**
 * Service that allows posting of events, which will be synchronously handled by subscribers to that type of event.
 */
public interface Publisher {

    /**
     * Publishes the given notification. Which will make it synchronously available to all subscribers to that notification's type.
     * This method will return after all Subscribers have handled it. 
     * If any handler throws an exception, the remaining notification handlers are not called,
     * and the exception propagates to the sender.
     *
     * @param notification
     * @param notificationDetails
     */
    void publish(Object notification, Object... notificationDetails);

    /**
     * Manually add a subscriber. Only use when you can not publish your subscriber as an OSGI service 
     */
    void addSubscriber(Subscriber subscriber);

	void addThreadSubscriber(Subscriber subscriber);

	void removeThreadSubscriber(Subscriber subscriber);
}
