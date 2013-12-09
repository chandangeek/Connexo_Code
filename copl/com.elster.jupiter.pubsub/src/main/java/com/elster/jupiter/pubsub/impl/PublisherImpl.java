package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@Component (name="com.elster.jupiter.pubsub", immediate = true )
public class PublisherImpl implements Publisher {

    private static final String FORMAT_KEY = "com.elster.jupiter.logging.format";
    private static final String DEFAULT_FORMAT = "%5$s";
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
	private final ThreadLocal<List<Subscription>> threadSubscriptionsHolder = new ThreadLocal<>();
    private volatile LogService logService;
    private volatile LogHandler handler;

    public PublisherImpl() {
    }

    public PublisherImpl(LogService logService) {
        setLogService(logService);
    }

    @Activate
    public void activate(Map<String, Object> props) {
        String format = DEFAULT_FORMAT;
        if (props != null && props.containsKey(FORMAT_KEY)) {
            format = (String) props.get(FORMAT_KEY);
        }
        handler = new LogHandler(logService, format);
        Logger.getLogger("").addHandler(handler);
    }

    @Deactivate
    public void deactivate() {
        Logger.getLogger("").removeHandler(handler);
    }

    @Override
	public void publish(Object event, Object... eventDetails) {
        for (Subscription each : subscriptions) {
			each.handle(event, eventDetails);
		}
        List<Subscription> threadSubscriptions = threadSubscriptionsHolder.get();
        if (threadSubscriptions == null || threadSubscriptions.isEmpty()) {
        	return;
        }
		for (Subscription each : threadSubscriptions) {
			each.handle(event, eventDetails);
		}		
	}

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addHandler(Subscriber subscriber) {
		this.subscriptions.add(new Subscription(subscriber));
	}
	
	public void removeHandler(Subscriber subscriber) {
		List<Subscription> toRemove = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            if (subscription.getSubscriber().equals(subscriber)) {
                toRemove.add(subscription);
            }
        }
        subscriptions.removeAll(toRemove);
    }

	@Override
	public void addThreadSubscriber(Subscriber subscriber) {
		List<Subscription> threadSubscriptions = threadSubscriptionsHolder.get();
		if (threadSubscriptions == null) {
			threadSubscriptions = new ArrayList<>();
			threadSubscriptionsHolder.set(threadSubscriptions);
		}
		threadSubscriptions.add(new Subscription(subscriber));
	}

	@Override
	public void removeThreadSubscriber(Subscriber subscriber) {
		List<Subscription> threadSubscriptions = threadSubscriptionsHolder.get();		
		if (threadSubscriptions != null) {
			Iterator<Subscription> it = threadSubscriptions.iterator();
			while (it.hasNext()) {
				if (it.next().getSubscriber().equals(subscriber)) {
					it.remove();
				}
			}			
		}		
	}
	
	@Reference
	public void setLogService(LogService logService) {
        this.logService = logService;
	}
}
