package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import com.elster.jupiter.util.time.Clock;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.EventPublisherImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (17:44)
 */
@RunWith(MockitoJUnitRunner.class)
public class EventPublisherImplTest {

    @Mock
    private Clock clock;
    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private EngineModelService engineModelService;

    @Test
    public void testRegisterNewReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);

        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business method
        eventPublisher.registerInterest(eventReceiver);

        // Asserts
        verify(factory).newFor(eventReceiver);
    }

    @Test
    public void testRegisterReceiverTwice () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);

        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business method
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.registerInterest(eventReceiver);

        // Asserts
        verify(factory).newFor(eventReceiver);
    }

    @Test
    public void testRegisterUnRegisterAndRegisterAgain () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);

        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business method
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.unregisterAllInterests(eventReceiver);
        eventPublisher.registerInterest(eventReceiver);

        // Asserts
        verify(factory, times(2)).newFor(eventReceiver);
    }

    @Test
    public void testNewlyRegisteredReceiversReceiveAllEventCategories () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);

        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business method
        eventPublisher.registerInterest(eventReceiver);

        // Asserts
        ArgumentCaptor<EnumSet> argumentCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(filteringEventReceiver).narrowTo(argumentCaptor.capture());
        EnumSet<Category> eventCategories = argumentCaptor.getValue();
        for (Category category : Category.values()) {
            assertThat(eventCategories.contains(category)).as("Category" + category + " not contained in narrow set").isTrue();
        }
    }

    @Test
    public void testRegisteringAndThenNarrowingToDeviceDelegatesNarrowingToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);
        BaseDevice device = mock(BaseDevice.class);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        List<BaseDevice> interestedDevices = Arrays.asList(device);
        eventPublisher.narrowInterestToDevices(eventReceiver, interestedDevices);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).narrowToDevices(interestedDevices);
    }

    @Test
    public void testWidenToAllDevicesOnRegisteredReceiverDelegatesWideningToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);
        BaseDevice device = mock(BaseDevice.class);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.widenInterestToAllDevices(eventReceiver);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).widenToAllDevices();
    }

    @Test
    public void testRegisteringAndThenNarrowingToConnectionTaskDelegatesNarrowingToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);
        ConnectionTask connectionTask = mock(ConnectionTask.class);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        List<ConnectionTask> interestedConnectionTasks = Arrays.asList(connectionTask);
        eventPublisher.narrowInterestToConnectionTasks(eventReceiver, interestedConnectionTasks);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).narrowToConnectionTasks(interestedConnectionTasks);
    }

    @Test
    public void testWidenToAllConnectionTasksOnRegisteredReceiverDelegatesWideningToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.widenInterestToAllConnectionTasks(eventReceiver);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).widenToAllConnectionTasks();
    }

    @Test
    public void testRegisteringAndThenNarrowingToComTaskDelegatesNarrowingToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        List<ComTaskExecution> interestedComTaskExecutions = Arrays.asList(comTaskExecution);
        eventPublisher.narrowInterestToComTaskExecutions(eventReceiver, interestedComTaskExecutions);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).narrowToComTaskExecutions(interestedComTaskExecutions);
    }

    @Test
    public void testWidenToAllComTasksOnRegisteredReceiverDelegatesWideningToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.widenInterestToAllComTaskExecutions(eventReceiver);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).widenToAllComTasks();
    }

    @Test
    public void testRegisteringAndThenNarrowingToComPortDelegatesNarrowingToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);
        ComPort comPort = mock(ComPort.class);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        List<ComPort> comPorts = Arrays.asList(comPort);
        eventPublisher.narrowInterestToComPorts(eventReceiver, comPorts);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).narrowToComPorts(comPorts);
    }

    @Test
    public void testWidenToAllComPortsOnRegisteredReceiverDelegatesWideningToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.widenInterestToAllComPorts(eventReceiver);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).widenToAllComPorts();
    }

    @Test
    public void testRegisteringAndThenNarrowingToComPortPoolDelegatesNarrowingToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);
        ComPortPool comPortPool = mock(ComPortPool.class);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        List<ComPortPool> comPortPools = Arrays.asList(comPortPool);
        eventPublisher.narrowInterestToComPortPools(eventReceiver, comPortPools);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).narrowToComPortPools(comPortPools);
    }

    @Test
    public void testWidenToAllComPortPoolsOnRegisteredReceiverDelegatesWideningToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.widenInterestToAllComPortPools(eventReceiver);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).widenToAllComPortPools();
    }

    @Test
    public void testRegisteringAndThenNarrowingToLogLevelDelegatesNarrowingToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);
        LogLevel expectedLogLevel = LogLevel.DEBUG;

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.narrowInterestToLogLevel(eventReceiver, expectedLogLevel);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).narrowToLogLevel(expectedLogLevel);
    }

    @Test
    public void testWidenToAllLogLevelsOnRegisteredReceiverDelegatesWideningToExistingFilteringEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiverFactory factory = mock(FilteringEventReceiverFactory.class);
        FilteringEventReceiver filteringEventReceiver = mock(FilteringEventReceiver.class);
        when(filteringEventReceiver.delegatesTo(eventReceiver)).thenReturn(true);
        when(factory.newFor(eventReceiver)).thenReturn(filteringEventReceiver);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService, factory);

        // Business methods
        eventPublisher.registerInterest(eventReceiver);
        eventPublisher.widenToAllLogLevels(eventReceiver);

        // Asserts
        verify(factory, times(1)).newFor(eventReceiver);
        verify(filteringEventReceiver).widenToAllLogLevels();
    }

    @Test
    public void doesNotPublishToNotInterestedEventReceivers () {
        EventReceiver receiverForConnectionEvents = mock(EventReceiver.class);
        EventReceiver receiverForLoggingEvents = mock(EventReceiver.class);
        EventPublisherImpl eventPublisher = new EventPublisherImpl(this.clock, this.engineModelService, this.deviceDataService);
        eventPublisher.narrowInterestToCategories(receiverForConnectionEvents, EnumSet.of(Category.CONNECTION));
        eventPublisher.narrowInterestToCategories(receiverForLoggingEvents, EnumSet.of(Category.LOGGING));
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business method
        eventPublisher.publish(event);

        // Asserts
        verify(receiverForConnectionEvents).receive(event);
        verify(receiverForLoggingEvents, never()).receive(event);
    }

}