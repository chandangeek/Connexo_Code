package com.elster.jupiter.pubsub;

/**
 * Service that allows posting of events, which will be synchronously handled by subscribers to that type of event.
 */
public interface Publisher {

    /**
     * Publishes the given event. Which will make it synchronously available to all subscribers to that event's type.
     * This method will return after all Subscribers have handled it. 
     * If any handler throws an exception, the remaining event handlers are not called,
     * and the exception propagates to the sender.
     *
     * @param event
     * @param eventDetails
     */
    void publish(Object event, Object... eventDetails);
	void addThreadSubscriber(Subscriber subscriber);
	void removeThreadSubscriber(Subscriber subscriber);
}
