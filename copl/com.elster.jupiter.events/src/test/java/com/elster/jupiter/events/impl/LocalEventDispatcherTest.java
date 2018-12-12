/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalEventDispatcherTest {

    private static final String TOPIC = "com.elster.jupiter.topic";
    @Mock
    private LocalEvent localEvent;
    @Mock
    private EventType eventType;
    @Mock
    private TopicHandler topicHandler;

    @Before
    public void setUp() {
        when(localEvent.getType()).thenReturn(eventType);
        when(eventType.getTopic()).thenReturn(TOPIC);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testOnEventWithOneSubscriberForLiteralMatch() {
        when(topicHandler.getTopicMatcher()).thenReturn(TOPIC);

        LocalEventDispatcher localEventDispatcher = new LocalEventDispatcher();

        localEventDispatcher.addSubscription(topicHandler);

        localEventDispatcher.onEvent(localEvent);

        verify(topicHandler).handle(localEvent);
    }

    @Test
    public void testOnEventWithOneSubscriberForWildcardMatch() {
        when(topicHandler.getTopicMatcher()).thenReturn("com.elster.jupiter.*");

        LocalEventDispatcher localEventDispatcher = new LocalEventDispatcher();

        localEventDispatcher.addSubscription(topicHandler);

        localEventDispatcher.onEvent(localEvent);

        verify(topicHandler).handle(localEvent);
    }

    @Test
    public void testOnEventWithNoMatchingSubscribers() {
        when(topicHandler.getTopicMatcher()).thenReturn("com.elster.jupiter.othertopic");

        LocalEventDispatcher localEventDispatcher = new LocalEventDispatcher();

        localEventDispatcher.addSubscription(topicHandler);

        localEventDispatcher.onEvent(localEvent);

        verify(topicHandler, never()).handle(localEvent);
    }

    @Test
    public void testOnEventWithTwoSubscribersForMixedMatching() {
        when(topicHandler.getTopicMatcher()).thenReturn("com.elster.jupiter.*", TOPIC);

        LocalEventDispatcher localEventDispatcher = new LocalEventDispatcher();

        localEventDispatcher.addSubscription(topicHandler);
        localEventDispatcher.addSubscription(topicHandler);

        localEventDispatcher.onEvent(localEvent);

        verify(topicHandler, times(2)).handle(localEvent);
    }

    @Test
    public void testRemoveSubscription() {
        when(topicHandler.getTopicMatcher()).thenReturn(TOPIC);
        LocalEventDispatcher localEventDispatcher = new LocalEventDispatcher();

        localEventDispatcher.addSubscription(topicHandler);
        localEventDispatcher.onEvent(localEvent);
        localEventDispatcher.removeSubscription(topicHandler);
        localEventDispatcher.onEvent(localEvent);

        verify(topicHandler, times(1)).handle(localEvent);
    }

}
