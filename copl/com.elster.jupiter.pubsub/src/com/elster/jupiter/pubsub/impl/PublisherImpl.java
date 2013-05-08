package com.elster.jupiter.pubsub.impl;

import java.util.*;
import org.osgi.service.component.annotations.*;
import com.elster.jupiter.pubsub.*;

@Component (name="com.elster.jupiter.pubsub", immediate = true )
public class PublisherImpl implements Publisher {
	
	private List<Subscription> subscriptions = new ArrayList<Subscription>();
	private final ThreadLocal<Subscriber> threadSubscribers = new ThreadLocal<>();

	@Override
	public void publish(Object event) {	
		for (Subscription each : getSubscriptions()) {
			each.handle(event);
		}
		Subscriber subscriber = threadSubscribers.get();
		if (subscriber != null) {
			subscriber.handle(event);
		}
	}

	synchronized private List<Subscription> getSubscriptions() {
		return new ArrayList<>(subscriptions);
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC) 
	synchronized public void addHandler(Subscriber subscriber,Map<String, Object> properties) {
		Object filter = properties.get(Subscriber.TOPIC);
		if (filter == null) {
			return;
		}
		Class<?>[] classes;
		if (filter instanceof Class<?>) {
			classes = new Class<?>[] { (Class<?>) filter };
		} else {
			classes = (Class<?>[]) filter;
		}
		this.subscriptions.add(new Subscription(subscriber, classes));
	}
	
	synchronized public void removeHandler(Subscriber subscriber) {
		Iterator<Subscription> it = subscriptions.iterator();
		while (it.hasNext()) {
			if (it.next().getSubscriber() == subscriber) {
				it.remove();				
			}
		}
	}

	@Override
	public void setThreadSubscriber(Subscriber subscriber) {		
		this.threadSubscribers.set(subscriber);
	}

	@Override
	public void unsetThreadSubscriber() {
		this.threadSubscribers.remove();		
	}
}
