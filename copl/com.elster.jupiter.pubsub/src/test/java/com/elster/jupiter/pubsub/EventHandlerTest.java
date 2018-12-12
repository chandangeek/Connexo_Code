/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pubsub;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {

    private EventHandler<String> eventHandler;

    @Mock
    private BundleContext context;

    @Before
    public void setUp() {
        eventHandler = spy(new EventHandler<String>(String.class) {
            @Override
            protected void onEvent(String event, Object... eventDetails) {
            }
        });
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testProperPropagationToOnEventMethod() {
        eventHandler.handle("event", "eventDetail1", "eventDetail2");

        verify(eventHandler).onEvent("event", "eventDetail1", "eventDetail2");
    }

    @Test
    public void testProperlyFilterTypeMismatches() {
        eventHandler.handle(17, "eventDetail1", "eventDetail2");

        verify(eventHandler, never()).onEvent(anyString(), eq("eventDetail1"), eq("eventDetail2"));
    }

}
