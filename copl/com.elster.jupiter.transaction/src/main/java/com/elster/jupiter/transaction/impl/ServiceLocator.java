package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.transaction.*;

public interface ServiceLocator {
	void publish(Object event);
	void addThreadSubscriber(Subscriber subscriber);
	void removeThreadSubscriber(Subscriber subscriber);	
}
