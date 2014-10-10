package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.AlreadyASubscriberForQueueException;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.InactiveDestinationException;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransientDestinationSpecTest {

    private static final String NAME = "name";
    private static final String RAW = "RAW";
    private static final String SUBSCRIBER = "SUBSCRIBER";

    private TransientDestinationSpec destinationSpec;

    @Mock
    private TransientQueueTableSpec queueTableSpec;
    @Mock
    private TransientSubscriberSpec subscriber1, subscriber2;
    @Mock
    private TransientSubscriberSpec subscriber;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat nlsMessageFormat;

    @Before
    public void setUp() throws Exception {
        when(queueTableSpec.isMultiConsumer()).thenReturn(true);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
        destinationSpec = new TransientDestinationSpec(queueTableSpec, thesaurus, NAME,false);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstruction() {
        assertThat(destinationSpec.getName()).isEqualTo(NAME);
        assertThat(destinationSpec.isActive()).isFalse();
        assertThat(destinationSpec.getQueueTableSpec()).isEqualTo(queueTableSpec);
    }

    @Test
    public void testIsTopicDelegatesToQueueTableSpec() {
        when(queueTableSpec.isMultiConsumer()).thenReturn(true, false);

        assertThat(destinationSpec.isTopic()).isTrue();
        assertThat(destinationSpec.isTopic()).isFalse();
    }

    @Test
    public void testIsQueueDelegatesToQueueTableSpec() {
        when(queueTableSpec.isMultiConsumer()).thenReturn(true, false);

        assertThat(destinationSpec.isQueue()).isFalse();
        assertThat(destinationSpec.isQueue()).isTrue();
    }

    @Test
    public void testGetPayloadTypeDelegatesToQueueTableSpec() {
        when(queueTableSpec.getPayloadType()).thenReturn(RAW);

        assertThat(destinationSpec.getPayloadType()).isEqualTo(RAW);
    }

    @Test(expected = InactiveDestinationException.class)
    public void testSubscribeOnInactiveDestinationThrowsException() {
        destinationSpec.subscribe(SUBSCRIBER);
    }

    @Test
    public void testActivationOnAQ() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();
    }

    @Test
    public void testDeactivate() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        destinationSpec.deactivate();

        assertThat(destinationSpec.isActive()).isFalse();
    }

    @Test
    public void testGetConsumers() {
        destinationSpec.activate();

        subscriber1 = (TransientSubscriberSpec) destinationSpec.subscribe("1");
        subscriber2 = (TransientSubscriberSpec) destinationSpec.subscribe("2");

        ImmutableList<SubscriberSpec> subscribers = ImmutableList.<SubscriberSpec>of(subscriber1, subscriber2);

        assertThat(destinationSpec.getSubscribers()).isEqualTo(subscribers);
    }

    @Test(expected = DuplicateSubscriberNameException.class)
    public void testSubscribeDuplicate() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        destinationSpec.activate();
        subscriber = (TransientSubscriberSpec) destinationSpec.subscribe(SUBSCRIBER);

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        destinationSpec.subscribe(SUBSCRIBER);
    }

    @Test(expected = AlreadyASubscriberForQueueException.class)
    public void testSubscribeMultipleOnQueue() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        when(queueTableSpec.isMultiConsumer()).thenReturn(false);
        destinationSpec.activate();
        subscriber = (TransientSubscriberSpec) destinationSpec.subscribe("A");

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        destinationSpec.subscribe(SUBSCRIBER + "2");
    }

    @Test
    public void testSubscribeSuccess() throws SQLException {
        when(queueTableSpec.isJms()).thenReturn(false);
        when(queueTableSpec.isMultiConsumer()).thenReturn(false);

        destinationSpec.activate();

        assertThat(destinationSpec.isActive()).isTrue();

        SubscriberSpec subscriberSpec = destinationSpec.subscribe(SUBSCRIBER);

        assertThat(subscriberSpec.getName()).isEqualTo(SUBSCRIBER);
        assertThat(subscriberSpec.getDestination()).isEqualTo(destinationSpec);
    }


}
