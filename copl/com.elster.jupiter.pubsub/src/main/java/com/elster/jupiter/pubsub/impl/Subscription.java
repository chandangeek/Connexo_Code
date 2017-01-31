/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pubsub.impl;

import java.util.Objects;

import com.elster.jupiter.pubsub.Subscriber;

class Subscription {
	private final Subscriber subscriber;
	private final Class<?>[] classes;
	
	Subscription(Subscriber subscriber) {
		this.subscriber = subscriber;
		this.classes = Objects.requireNonNull(subscriber.getClasses());
	}
	
	void handle(Object event, Object... eventDetails) {
		for (Class<?> clazz : classes) {
			if (clazz.isInstance(event)) {
				subscriber.handle(event, eventDetails);
				return;
			}
		}
	}
	
	Subscriber getSubscriber() {
		return subscriber;
	}
}
