package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component (name="com.elster.jupiter.pubsub", immediate = true )
public class PublisherImpl implements Publisher {
	
	private List<Subscription> subscriptions = new ArrayList<>();
	private final ThreadLocal<Subscriber> threadSubscribers = new ThreadLocal<>();

	@Override
	public void publish(Object event, Object... eventDetails) {
		for (Subscription each : getSubscriptions()) {
			each.handle(event, eventDetails);
		}
		Subscriber subscriber = threadSubscribers.get();
		if (subscriber != null) {
			subscriber.handle(event, eventDetails);
		}
	}

	synchronized private List<Subscription> getSubscriptions() {
		return new ArrayList<>(subscriptions);
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC) 
	synchronized public void addHandler(Subscriber subscriber, Map<String, Object> properties) {
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
	
	@Reference
	public void setLogService(LogService logService) {
		//LogManager.getLogManager().reset();
		Logger.getLogger("").addHandler(new LogHandler(logService));
	}
}
