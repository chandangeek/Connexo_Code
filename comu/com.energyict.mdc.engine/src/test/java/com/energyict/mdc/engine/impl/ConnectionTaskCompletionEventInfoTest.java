package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that the {@link EventService} will be able to publish
 * the {@link ConnectionTaskCompletionEventInfo} component correctly.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-16 (14:41)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTaskCompletionEventInfoTest {

    private static final long CONNECTION_TASK_ID = 97L;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    private static final long COMSERVER_ID = DEVICE_ID + 1;
    private static final long COMPORT_ID = COMSERVER_ID + 1;
    private static final long COMTASK_EXECUTION_1_ID = COMPORT_ID + 1;
    private static final long COMTASK_EXECUTION_2_ID = COMTASK_EXECUTION_1_ID + 1;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Device device;
    @Mock
    private ScheduledConnectionTask connectionTask;
    @Mock
    private ComServer comServer;
    @Mock
    private ComPort comPort;
    @Mock
    private ScheduledComTaskExecution comTaskExecution1;
    @Mock
    private ScheduledComTaskExecution comTaskExecution2;

    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;
    private EventService eventService;

    @Before
    public void setup() {
        this.bootstrapModule = new InMemoryBootstrapModule();
        this.injector = Guice.createInjector(
                new MockModule(),
                this.bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(false),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new UserModule());
        TransactionService transactionService = this.injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            this.eventService = this.injector.getInstance(EventService.class);
            for (EventType eventType : EventType.values()) {
                eventType.install(this.eventService);
            }
            ctx.commit();
        }

    }

    @After
    public void tearDown () {
        this.bootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() {
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(this.connectionTask.getDevice()).thenReturn(this.device);
        when(this.comPort.getComServer()).thenReturn(this.comServer);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.comTaskExecution1.getId()).thenReturn(COMTASK_EXECUTION_1_ID);
        when(this.comTaskExecution1.getDevice()).thenReturn(this.device);
        when(this.comTaskExecution2.getId()).thenReturn(COMTASK_EXECUTION_2_ID);
        when(this.comTaskExecution2.getDevice()).thenReturn(this.device);
    }

    @Test
    public void testPostEvent () {
        ConnectionTaskCompletionEventInfo eventInfo = ConnectionTaskCompletionEventInfo.forCompletion(
                this.connectionTask,
                this.comPort,
                Arrays.asList(this.comTaskExecution1, this.comTaskExecution2),
                Arrays.asList(this.comTaskExecution2, this.comTaskExecution1),
                Collections.emptyList());

        // Business method
        this.eventService.postEvent(EventType.DEVICE_CONNECTION_COMPLETION.topic(), eventInfo);

        // Asserts: not expecting any errors
        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(this.eventAdmin).postEvent(eventArgumentCaptor.capture());
        Event event = eventArgumentCaptor.getValue();
        assertThat(event.containsProperty("timestamp")).isTrue();
        assertThat(event.getProperty("event.topics")).isEqualTo(EventType.DEVICE_CONNECTION_COMPLETION.topic());
        assertThat(event.getProperty("deviceId")).isEqualTo(this.device.getId());
        assertThat(event.getProperty("comPortId")).isEqualTo(this.comPort.getId());
        assertThat(event.getProperty("comServerId")).isEqualTo(this.comServer.getId());
        assertThat(event.getProperty("connectionTaskId")).isEqualTo(this.connectionTask.getId());
        assertThat(event.getProperty("successTaskIDs")).isEqualTo(String.valueOf(this.comTaskExecution1.getId()) + "," + String.valueOf(this.comTaskExecution2.getId()));
        assertThat(event.getProperty("failedTaskIDs")).isEqualTo(String.valueOf(this.comTaskExecution2.getId()) + "," + String.valueOf(this.comTaskExecution1.getId()));
        assertThat(event.getProperty("skippedTaskIDs")).isEqualTo("");
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
        }

    }

}