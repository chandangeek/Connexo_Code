package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.pubsub.Subscriber;

public interface ServiceLocator {
	void publish(Object event);
	void addThreadSubscriber(Subscriber subscriber);
	void removeThreadSubscriber(Subscriber subscriber);	
}
