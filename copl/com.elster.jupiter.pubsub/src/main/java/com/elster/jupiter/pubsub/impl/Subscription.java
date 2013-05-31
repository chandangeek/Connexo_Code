package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Subscriber;

class Subscription {
	private final Subscriber subscriber;
	private final Class<?>[] classes;
	
	Subscription(Subscriber subscriber , Class<?> ... classes) {
		this.subscriber = subscriber;
		this.classes = classes;
	}
	
	void handle(Object event) {
		for (Class<?> clazz : classes) {
			if (clazz.isInstance(event)) {
				subscriber.handle(event);
				return;
			}
		}
	}
	
	Subscriber getSubscriber() {
		return subscriber;
	}
}
