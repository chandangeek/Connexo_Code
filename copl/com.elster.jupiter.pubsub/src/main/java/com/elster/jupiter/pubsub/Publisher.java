package com.elster.jupiter.pubsub;

public interface Publisher {

    void publish(Object event, Object... eventDetails);

    void setThreadSubscriber(Subscriber subscriber);

    void unsetThreadSubscriber();
}
