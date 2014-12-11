package com.elster.jupiter.pubsub.impl;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.pubsub.Subscriber;

@RunWith(MockitoJUnitRunner.class)
public class PublisherImplTest {

    private Subscriber subscriber;
    
    @Before
    public void setUp() {
        subscriber = spy(new Subscriber() {
       
			@Override
			public void handle(Object notification, Object... notificationDetails) {
			}

			@Override
			public Class<?>[] getClasses() {
				return new Class<?>[] {String.class}; 
			}
        });
    }

    @Test
    public void testSubscriberReceivesEventsOfRegisteredType() {
        PublisherImpl publisher = new PublisherImpl();
        publisher.addHandler(subscriber);

        publisher.publish("A", "B");

        verify(subscriber).handle("A", "B");
    }

    @Test
    public void testThreadSubscribersReceivesEvents() {
        PublisherImpl publisher = new PublisherImpl();
        try {
            publisher.addThreadSubscriber(subscriber);

            publisher.publish("A", "B");

            verify(subscriber).handle("A", "B");
        } finally {
            publisher.removeThreadSubscriber(subscriber);
        }
    }

    @Test
    public void testRemoveHandlerEnsuresItNoLongerReceivesEvents() {
        PublisherImpl publisher = new PublisherImpl();
        publisher.addHandler(subscriber);

        publisher.publish("A", "B");
        publisher.removeHandler(subscriber);
        publisher.publish("A", "B");

        verify(subscriber, atMost(1)).handle("A", "B");
    }

    @Test
    public void testSubscriberReceivesEventsOfMultipleRegisteredTypes() {
        PublisherImpl publisher = new PublisherImpl();
        publisher.addHandler(subscriber);

        publisher.publish("A", "B");
        publisher.publish(1, "B");

        verify(subscriber).handle("A", "B");
        verify(subscriber,never()).handle(1, "B");
    }

    @Test
    public void testSubscriberDoesNotReceiveEventsOfNotRegisteredType() {
        PublisherImpl publisher = new PublisherImpl();
        publisher.addHandler(subscriber);
        publisher.publish(1, "B");
        verify(subscriber, never()).handle(1, "B");
    }


}
