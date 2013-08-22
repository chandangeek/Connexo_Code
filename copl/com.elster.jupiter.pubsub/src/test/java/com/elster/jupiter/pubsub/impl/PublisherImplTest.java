package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Subscriber;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PublisherImplTest {

    @Mock
    private Subscriber subscriber;

    @Test
    public void testSubscriberReceivesEventsOfRegisteredType() {
        Map<String, Object> map = ImmutableMap.<String, Object>of(Subscriber.TOPIC, String.class);
        PublisherImpl publisher = new PublisherImpl();
        publisher.addHandler(subscriber, map);

        publisher.publish("A", "B");

        verify(subscriber).handle("A", "B");
    }

    @Test
    public void testThreadSubscribersReceivesEvents() {
        PublisherImpl publisher = new PublisherImpl();
        try {
            publisher.setThreadSubscriber(subscriber);

            publisher.publish("A", "B");

            verify(subscriber).handle("A", "B");
        } finally {
            publisher.unsetThreadSubscriber();
        }
    }

    @Test
    public void testRemoveHandlerEnsuresItNoLongerReceivesEvents() {
        Map<String, Object> map = ImmutableMap.<String, Object>of(Subscriber.TOPIC, String.class);
        PublisherImpl publisher = new PublisherImpl();
        publisher.addHandler(subscriber, map);

        publisher.publish("A", "B");
        publisher.removeHandler(subscriber);
        publisher.publish("A", "B");

        verify(subscriber, atMost(1)).handle("A", "B");
    }

    @Test
    public void testSubscriberReceivesEventsOfMultipleRegisteredTypes() {
        Map<String, Object> map = ImmutableMap.<String, Object>of(Subscriber.TOPIC, new Class<?>[] {String.class, Integer.class});
        PublisherImpl publisher = new PublisherImpl();
        publisher.addHandler(subscriber, map);

        publisher.publish("A", "B");
        publisher.publish(1, "B");

        verify(subscriber).handle("A", "B");
        verify(subscriber).handle(1, "B");
    }

    @Test
    public void testSubscriberDoesNotReceiveEventsOfNotRegisteredType() {
        Map<String, Object> map = ImmutableMap.<String, Object>of(Subscriber.TOPIC, String.class);
        PublisherImpl publisher = new PublisherImpl();
        publisher.addHandler(subscriber, map);

        publisher.publish(1, "B");

        verify(subscriber, never()).handle(1, "B");
    }


}
