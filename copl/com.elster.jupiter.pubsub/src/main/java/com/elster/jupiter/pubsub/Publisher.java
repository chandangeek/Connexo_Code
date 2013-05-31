package com.elster.jupiter.pubsub;

public interface Publisher {
	void publish(Object event);
	void setThreadSubscriber(Subscriber subscriber);
	void unsetThreadSubscriber();
}
