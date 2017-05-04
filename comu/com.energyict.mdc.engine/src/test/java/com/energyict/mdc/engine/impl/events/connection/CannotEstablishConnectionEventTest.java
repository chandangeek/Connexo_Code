/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.nls.MessageSeed;
import com.energyict.mdc.upl.nls.NlsMessageFormat;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;

import com.energyict.protocol.exceptions.ConnectionException;
import org.joda.time.DateTime;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.connection.CannotEstablishConnectionEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (14:59)
 */
@RunWith(MockitoJUnitRunner.class)
public class CannotEstablishConnectionEventTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final String THESAURUS_ID = "ForTestingPurposesOnly";

    @Mock
    public Clock clock;
    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(this.clock.instant()).thenReturn(new DateTime(1969, 5, 2, 1, 40, 0).toDate().toInstant()); // Set some default
        when(this.serviceProvider.clock()).thenReturn(this.clock);
    }

    @Test
    public void testCategory () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testCategory")));

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Clock frozenClock = Clock.fixed(new DateTime(2012, Calendar.NOVEMBER, 6, 13, 45, 17, 0).toDate().toInstant(),ZoneId.systemDefault());
        Instant now = frozenClock.instant();
        when(this.clock.instant()).thenReturn(now);

        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method
        Instant timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsFailure () {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        String expectedTranslation = "Translation for Test message seed";
        when(messageFormat.format(anyVararg())).thenReturn(expectedTranslation);
        Thesaurus thesaurus = mock(Thesaurus.class);
        when(thesaurus.getFormat(TestMessageSeeds.TEST)).thenReturn(messageFormat);
        NlsService nlsService = mock(NlsService.class);
        when(nlsService.getThesaurus(THESAURUS_ID)).thenReturn(thesaurus);
        Services.nlsService(nlsService);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method & asserts
        assertThat(event.isFailure()).isTrue();
        verify(nlsService).getThesaurus(THESAURUS_ID);
        assertThat(event.getFailureMessage()).contains(expectedTranslation);
    }

    @Test
    public void testIsFailureWithoutNlsService () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method & asserts
        assertThat(event.isFailure()).isTrue();
        assertThat(event.getFailureMessage()).contains(TestMessageSeeds.TEST.getDefaultFormat());
    }

    @Test
    public void testIsNotEstablishing () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testIsNotEstablishing")));

        // Business method & asserts
        assertThat(event.isEstablishing()).isFalse();
    }

    @Test
    public void testIsNotClosed () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testIsNotClosed")));

        // Business method & asserts
        assertThat(event.isClosed()).isFalse();
    }

    @Test
    public void testIsNotLoggingRelated () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testIsNotEstablishing")));

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskRelated () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testIsNotComTaskRelated")));

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelated () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testIsNotComPortRelated")));

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelated () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testIsNotComPortPoolRelated")));

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testIsNotConnectionTaskRelated")));

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelated () {
        CannotEstablishConnectionEvent event =
                new CannotEstablishConnectionEvent(
                        this.serviceProvider,
                        null, null,
                        new ConnectionException(
                                THESAURUS_ID,
                                TestMessageSeeds.TEST,
                                new ForTestingPurposesOnly("testIsNotDeviceRelated")));

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        OutboundComPort comPort = mock(OutboundComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPorts () {
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsConnectionTaskRelated () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void testIsDeviceRelated () {
        Device device = mock(Device.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CannotEstablishConnectionEvent event = new CannotEstablishConnectionEvent(this.serviceProvider, comPort, connectionTask, new ConnectionException(THESAURUS_ID, TestMessageSeeds.TEST, new Exception()));

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    private class ForTestingPurposesOnly extends Exception {
        private ForTestingPurposesOnly(String testCategory) {
            super("For testing purposes only: " + testCategory, new Exception());
        }
    }

    private enum TestMessageSeeds implements MessageSeed {
        TEST;

        @Override
        public String getKey() {
            return "TEST";
        }

        @Override
        public String getDefaultFormat() {
            return "For testing purposes only";
        }

        @Override
        public int getId() {
            return ordinal();
        }

        @Override
        public Level getLevel() {
            return Level.SEVERE;
        }
    }
}