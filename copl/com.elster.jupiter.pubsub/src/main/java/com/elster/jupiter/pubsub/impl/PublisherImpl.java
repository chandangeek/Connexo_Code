/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.util.Registration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component (name="com.elster.jupiter.pubsub", immediate = true )
public class PublisherImpl implements Publisher {

    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
	private final ThreadLocal<List<Subscription>> threadSubscriptionsHolder = ThreadLocal.withInitial(ArrayList::new);

    public PublisherImpl() {
    }

    @Override
	public void publish(Object notification, Object... notificationDetails) {
        for (Subscription each : subscriptions) {
			each.handle(notification, notificationDetails);
		}
        List<Subscription> threadSubscriptions = threadSubscriptionsHolder.get();
        if (threadSubscriptions == null || threadSubscriptions.isEmpty()) {
        	return;
        }
		for (Subscription each : threadSubscriptions) {
			each.handle(notification, notificationDetails);
		}		
	}

    @Override
    public Registration addSubscriber(Subscriber subscriber) {
    	addHandler(subscriber);
        return () -> removeHandler(subscriber);
    }
    
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addHandler(Subscriber subscriber) {
		this.subscriptions.add(new Subscription(subscriber));
	}
	
	public void removeHandler(Subscriber subscriber) {
		subscriptions.removeIf(subscription -> subscription.getSubscriber().equals(subscriber));        		
    }

	@Override
	public Registration addThreadSubscriber(Subscriber subscriber) {
		Subscription subscription = new Subscription(subscriber);
		threadSubscriptionsHolder.get().add(subscription);
		return () -> removeThreadSubscriber(subscription);
	}

	private void removeThreadSubscriber(Subscription subscription) {
		threadSubscriptionsHolder.get().remove(subscription);
	}
	
}
