package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@Component (name="com.elster.jupiter.pubsub", immediate = true )
public class PublisherImpl implements Publisher {
	
	private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
	private final ThreadLocal<List<Subscription>> threadSubscriptionsHolder = new ThreadLocal<>();

    public PublisherImpl() {
    }

    public PublisherImpl(LogService logService) {
        setLogService(logService);
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
		Logger.getLogger("").addHandler(new LogHandler(logService));
	}
}
