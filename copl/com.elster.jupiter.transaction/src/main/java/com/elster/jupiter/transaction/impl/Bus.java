package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.pubsub.Subscriber;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

enum Bus {
    ;

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }


	static void publish(Object event) {
		locatorHolder.get().publish(event);
	}

    public static void addThreadSubscriber(Subscriber subscriber) {
        locatorHolder.get().addThreadSubscriber(subscriber);

    }

    public static void removeThreadSubscriber(Subscriber subscriber) {
        locatorHolder.get().removeThreadSubscriber(subscriber);
    }

}
