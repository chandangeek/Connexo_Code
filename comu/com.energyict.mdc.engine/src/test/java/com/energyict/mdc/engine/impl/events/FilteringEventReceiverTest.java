/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ComPortRelatedEvent;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.ComTaskExecutionEvent;
import com.energyict.mdc.engine.events.ConnectionEvent;
import com.energyict.mdc.engine.events.ConnectionTaskRelatedEvent;
import com.energyict.mdc.engine.events.DeviceRelatedEvent;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.FilteringEventReceiverImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (10:57)
 */
public class FilteringEventReceiverTest {

    @Test
    public void testDelegatesToEventReceiverFromConstructor () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);

        // Business method and assert
        assertThat(filteringEventReceiver.delegatesTo(eventReceiver)).isTrue();
    }

    @Test
    public void testDoesNotDelegateToAnotherEventReceiver () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        EventReceiver otherEventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);

        // Business method and assert
        assertThat(filteringEventReceiver.delegatesTo(otherEventReceiver)).isFalse();
    }

    @Test
    public void testFilterWithoutCriteria () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComServerEvent event = mock(ComServerEvent.class);

        // Business method
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToConnectionEventsWithOnlyConnectionEventsBeingPublished () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ConnectionEvent event = mock(ConnectionEvent.class);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business methods
        filteringEventReceiver.narrowTo(EnumSet.of(Category.CONNECTION));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToConnectionEventsWithOtherEventCategoriesBeingPublished () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.getCategory()).thenReturn(Category.COMTASK);

        // Business methods
        filteringEventReceiver.narrowTo(EnumSet.of(Category.CONNECTION));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testChangeNarrowFromConnectionEventsToAllEvents () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComServerEvent connectionEvent = mock(ComServerEvent.class);
        when(connectionEvent.getCategory()).thenReturn(Category.CONNECTION);
        ComServerEvent comTaskEvent = mock(ComServerEvent.class);
        when(comTaskEvent.getCategory()).thenReturn(Category.COMTASK);

        // Business methods
        filteringEventReceiver.narrowTo(EnumSet.of(Category.CONNECTION));
        filteringEventReceiver.narrowTo(EnumSet.allOf(Category.class));
        filteringEventReceiver.receive(connectionEvent);
        filteringEventReceiver.receive(comTaskEvent);

        // Asserts
        verify(eventReceiver).receive(comTaskEvent);
        verify(eventReceiver).receive(connectionEvent);
    }

    @Test
    public void testNarrowFromAllCategoriesToConnectionEventsOnly () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComServerEvent connectionEvent = mock(ComServerEvent.class);
        when(connectionEvent.getCategory()).thenReturn(Category.CONNECTION);
        ComServerEvent comTaskEvent = mock(ComServerEvent.class);
        when(comTaskEvent.getCategory()).thenReturn(Category.COMTASK);

        // Business methods
        filteringEventReceiver.narrowTo(EnumSet.allOf(Category.class));
        filteringEventReceiver.narrowTo(EnumSet.of(Category.CONNECTION));
        filteringEventReceiver.receive(connectionEvent);
        filteringEventReceiver.receive(comTaskEvent);

        // Asserts
        verify(eventReceiver).receive(connectionEvent);
        verify(eventReceiver, never()).receive(comTaskEvent);
    }

    @Test
    public void testNarrowToDeviceWithEventsForTheSameDevice () {
        Device device = mock(Device.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        DeviceRelatedEvent event = mock(DeviceRelatedEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getCategory()).thenReturn(Category.CONNECTION);
        when(event.getDevice()).thenReturn(device);

        // Business methods
        filteringEventReceiver.narrowToDevices(Arrays.asList(device));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToDeviceWithEventsForAnotherDevice () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(99l);
        Device otherDevice = mock(Device.class);
        when(otherDevice.getId()).thenReturn(522l);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        DeviceRelatedEvent event = mock(DeviceRelatedEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(otherDevice);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business methods
        filteringEventReceiver.narrowToDevices(Arrays.asList(device));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testNarrowToOtherDevices () {
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(1L);
        Device device2 = mock(Device.class);
        when(device2.getId()).thenReturn(2L);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        DeviceRelatedEvent eventForDevice1 = mock(DeviceRelatedEvent.class);
        when(eventForDevice1.isDeviceRelated()).thenReturn(true);
        when(eventForDevice1.getDevice()).thenReturn(device1);
        when(eventForDevice1.getCategory()).thenReturn(Category.CONNECTION);
        DeviceRelatedEvent eventForDevice2 = mock(DeviceRelatedEvent.class);
        when(eventForDevice2.isDeviceRelated()).thenReturn(true);
        when(eventForDevice2.getDevice()).thenReturn(device2);
        when(eventForDevice2.getCategory()).thenReturn(Category.CONNECTION);

        // Business methods
        filteringEventReceiver.narrowToDevices(Arrays.asList(device1));
        filteringEventReceiver.narrowToDevices(Arrays.asList(device2));
        filteringEventReceiver.receive(eventForDevice1);
        filteringEventReceiver.receive(eventForDevice2);

        // Asserts
        verify(eventReceiver, never()).receive(eventForDevice1);
        verify(eventReceiver).receive(eventForDevice2);
    }

    @Test
    public void testWidenToAllDevices () {
        Device device = mock(Device.class);
        Device otherDevice = mock(Device.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        DeviceRelatedEvent event = mock(DeviceRelatedEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(otherDevice);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business methods
        filteringEventReceiver.narrowToDevices(Arrays.asList(device));
        filteringEventReceiver.widenToAllDevices();
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testWidenToAllDevicesWithoutDeviceFilter () {
        Device device = mock(Device.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        DeviceRelatedEvent event = mock(DeviceRelatedEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(device);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business methods
        filteringEventReceiver.widenToAllDevices();
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToDeviceAndConnectionCategory () {
        Device device = mock(Device.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        DeviceRelatedEvent event = mock(DeviceRelatedEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(device);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business methods
        filteringEventReceiver.narrowTo(EnumSet.of(Category.CONNECTION));
        filteringEventReceiver.narrowToDevices(Arrays.asList(device));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToDeviceAndConnectionCategoryButForAnotherDevice () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        Device otherDevice = mock(Device.class);
        when(otherDevice.getId()).thenReturn(2L);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        DeviceRelatedEvent event = mock(DeviceRelatedEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(otherDevice);
        when(event.getCategory()).thenReturn(Category.CONNECTION);

        // Business methods
        filteringEventReceiver.narrowTo(EnumSet.of(Category.CONNECTION));
        filteringEventReceiver.narrowToDevices(Arrays.asList(device));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testNarrowToConnectionTaskWithEventsForTheSameConnectionTask () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ConnectionTaskRelatedEvent event = mock(ConnectionTaskRelatedEvent.class);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.getCategory()).thenReturn(Category.CONNECTION);
        when(event.getConnectionTask()).thenReturn(connectionTask);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(connectionTask));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToConnectionTaskWithEventsForAnotherConnectionTask () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(170l);
        when(connectionTask.getDevice()).thenReturn(device);
        ConnectionTask otherConnectionTask = mock(ConnectionTask.class);
        when(otherConnectionTask.getId()).thenReturn(933l);
        when(otherConnectionTask.getDevice()).thenReturn(device);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ConnectionTaskRelatedEvent event = mock(ConnectionTaskRelatedEvent.class);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.getCategory()).thenReturn(Category.CONNECTION);
        when(event.getConnectionTask()).thenReturn(otherConnectionTask);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(connectionTask));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testNarrowToOtherConnectionTasksOfTheSameDevice () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(145l);
        when(connectionTask.getDevice()).thenReturn(device);
        ConnectionTask otherConnectionTask = mock(ConnectionTask.class);
        when(otherConnectionTask.getId()).thenReturn(899l);
        when(otherConnectionTask.getDevice()).thenReturn(device);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ConnectionTaskRelatedEvent notExpected = mock(ConnectionTaskRelatedEvent.class);
        when(notExpected.isConnectionTaskRelated()).thenReturn(true);
        when(notExpected.getCategory()).thenReturn(Category.CONNECTION);
        when(notExpected.getConnectionTask()).thenReturn(connectionTask);
        ConnectionTaskRelatedEvent expected = mock(ConnectionTaskRelatedEvent.class);
        when(expected.isConnectionTaskRelated()).thenReturn(true);
        when(expected.getCategory()).thenReturn(Category.CONNECTION);
        when(expected.getConnectionTask()).thenReturn(otherConnectionTask);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(connectionTask));
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(otherConnectionTask));
        filteringEventReceiver.receive(notExpected);
        filteringEventReceiver.receive(expected);

        // Asserts
        verify(eventReceiver).receive(expected);
        verify(eventReceiver, never()).receive(notExpected);
    }

    @Test
    public void testWidenToAllConnectionTasks () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(145l);
        when(connectionTask.getDevice()).thenReturn(device);
        ConnectionTask otherConnectionTask = mock(ConnectionTask.class);
        when(otherConnectionTask.getId()).thenReturn(899l);
        when(otherConnectionTask.getDevice()).thenReturn(device);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ConnectionTaskRelatedEvent event = mock(ConnectionTaskRelatedEvent.class);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.getCategory()).thenReturn(Category.CONNECTION);
        when(event.getConnectionTask()).thenReturn(otherConnectionTask);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(connectionTask));
        filteringEventReceiver.widenToAllConnectionTasks();
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testWidenToAllConnectionTasksWithoutConnectionTaskFilter () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ConnectionTaskRelatedEvent event = mock(ConnectionTaskRelatedEvent.class);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.getCategory()).thenReturn(Category.CONNECTION);
        when(event.getConnectionTask()).thenReturn(connectionTask);

        // Business methods
        filteringEventReceiver.widenToAllConnectionTasks();
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToComTaskWithEventsForTheSameComTask () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.getCategory()).thenReturn(Category.COMTASK);
        when(event.getComTaskExecution()).thenReturn(comTaskExecution);
        when(event.getConnectionTask()).thenReturn(connectionTask);

        // Business methods
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(comTaskExecution));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToComTaskWithEventsForAnotherComTask () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(1L);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(1L);
        ComTaskExecution otherComTaskExecution = mock(ComTaskExecution.class);
        when(otherComTaskExecution.getId()).thenReturn(2L);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.getCategory()).thenReturn(Category.COMTASK);
        when(event.getComTaskExecution()).thenReturn(otherComTaskExecution);
        when(event.getConnectionTask()).thenReturn(connectionTask);

        // Business methods
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(comTaskExecution));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testNarrowToComTaskAndConnectionTaskWithEventsForAnotherConnectionTaskOfTheSameDevice () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(33l);
        when(connectionTask.getDevice()).thenReturn(device);
        ConnectionTask otherConnectionTask = mock(ConnectionTask.class);
        when(otherConnectionTask.getId()).thenReturn(654l);
        when(otherConnectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.isComTaskExecutionRelated()).thenReturn(true);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.getCategory()).thenReturn(Category.CONNECTION);
        when(event.getComTaskExecution()).thenReturn(comTaskExecution);
        when(event.getConnectionTask()).thenReturn(otherConnectionTask);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(connectionTask));
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(comTaskExecution));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testNarrowToComTaskAndConnectionTaskWithEventsForAnotherConnectionTaskOfAnotherDevice () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        Device otherDevice = mock(Device.class);
        when(otherDevice.getId()).thenReturn(2L);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(1L);
        when(connectionTask.getDevice()).thenReturn(device);
        ConnectionTask otherConnectionTask = mock(ConnectionTask.class);
        when(otherConnectionTask.getId()).thenReturn(2L);
        when(otherConnectionTask.getDevice()).thenReturn(otherDevice);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.isComTaskExecutionRelated()).thenReturn(true);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.getCategory()).thenReturn(Category.CONNECTION);
        when(event.getComTaskExecution()).thenReturn(comTaskExecution);
        when(event.getConnectionTask()).thenReturn(otherConnectionTask);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(connectionTask));
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(comTaskExecution));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testNarrowToComTaskConnectionTaskAndComPort () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComPort comPort = mock(ComPort.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.isComTaskExecutionRelated()).thenReturn(true);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.isComPortRelated()).thenReturn(true);
        when(event.getCategory()).thenReturn(Category.CONNECTION);
        when(event.getComTaskExecution()).thenReturn(comTaskExecution);
        when(event.getConnectionTask()).thenReturn(connectionTask);
        when(event.getComPort()).thenReturn(comPort);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(connectionTask));
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(comTaskExecution));
        filteringEventReceiver.narrowToComPorts(Arrays.asList(comPort));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToComTaskConnectionTaskAndComPortWithEventsForAnotherComTask () {
        Device device = mock(Device.class);
        ConnectionTask interestedConnectionTask = mock(ConnectionTask.class);
        when(interestedConnectionTask.getId()).thenReturn(1l);
        when(interestedConnectionTask.getDevice()).thenReturn(device);
        ComTaskExecution interestedComTaskExecution = mock(ComTaskExecution.class);
        when(interestedComTaskExecution.getId()).thenReturn(1l);
        ComTaskExecution otherComTaskExecution = mock(ComTaskExecution.class);
        when(otherComTaskExecution.getId()).thenReturn(2l);
        ComPort interestedComPort = mock(ComPort.class);
        when(interestedComPort.getId()).thenReturn(1l);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent notExpected = mock(ComTaskExecutionEvent.class);
        when(notExpected.isComTaskExecutionRelated()).thenReturn(true);
        when(notExpected.isConnectionTaskRelated()).thenReturn(true);
        when(notExpected.isComPortRelated()).thenReturn(true);
        when(notExpected.getCategory()).thenReturn(Category.CONNECTION);
        when(notExpected.getComTaskExecution()).thenReturn(otherComTaskExecution);
        when(notExpected.getConnectionTask()).thenReturn(interestedConnectionTask);
        when(notExpected.getComPort()).thenReturn(interestedComPort);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(interestedConnectionTask));
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(interestedComTaskExecution));
        filteringEventReceiver.narrowToComPorts(Arrays.asList(interestedComPort));
        filteringEventReceiver.receive(notExpected);

        // Asserts
        verify(eventReceiver, never()).receive(notExpected);
    }

    @Test
    public void testNarrowToComTaskConnectionTaskAndComPortWithEventsForAnotherConnectionTask () {
        Device device = mock(Device.class);
        ConnectionTask interestedConnectionTask = mock(ConnectionTask.class);
        when(interestedConnectionTask.getId()).thenReturn(1L);
        when(interestedConnectionTask.getDevice()).thenReturn(device);
        ConnectionTask otherConnectionTask = mock(ConnectionTask.class);
        when(otherConnectionTask.getId()).thenReturn(2L);
        when(otherConnectionTask.getDevice()).thenReturn(device);
        ComTaskExecution interestedComTaskExecution = mock(ComTaskExecution.class);
        when(interestedComTaskExecution.getId()).thenReturn(1L);
        ComPort interestedComPort = mock(ComPort.class);
        when(interestedComPort.getId()).thenReturn(1L);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent notExpected = mock(ComTaskExecutionEvent.class);
        when(notExpected.isComTaskExecutionRelated()).thenReturn(true);
        when(notExpected.isConnectionTaskRelated()).thenReturn(true);
        when(notExpected.isComPortRelated()).thenReturn(true);
        when(notExpected.getCategory()).thenReturn(Category.CONNECTION);
        when(notExpected.getComTaskExecution()).thenReturn(interestedComTaskExecution);
        when(notExpected.getConnectionTask()).thenReturn(otherConnectionTask);
        when(notExpected.getComPort()).thenReturn(interestedComPort);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(interestedConnectionTask));
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(interestedComTaskExecution));
        filteringEventReceiver.narrowToComPorts(Arrays.asList(interestedComPort));
        filteringEventReceiver.receive(notExpected);

        // Asserts
        verify(eventReceiver, never()).receive(notExpected);
    }

    @Test
    public void testNarrowToComTaskConnectionTaskAndComPortWithEventsForAnotherComPort () {
        Device device = mock(Device.class);
        ConnectionTask interestedConnectionTask = mock(ConnectionTask.class);
        when(interestedConnectionTask.getId()).thenReturn(1l);
        when(interestedConnectionTask.getDevice()).thenReturn(device);
        ComTaskExecution interestedComTaskExecution = mock(ComTaskExecution.class);
        when(interestedComTaskExecution.getId()).thenReturn(1l);
        ComPort interestedComPort = mock(ComPort.class);
        when(interestedComPort.getId()).thenReturn(1l);
        ComPort otherComPort = mock(ComPort.class);
        when(otherComPort.getId()).thenReturn(2l);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent notExpected = mock(ComTaskExecutionEvent.class);
        when(notExpected.isComTaskExecutionRelated()).thenReturn(true);
        when(notExpected.isConnectionTaskRelated()).thenReturn(true);
        when(notExpected.isComPortRelated()).thenReturn(true);
        when(notExpected.getCategory()).thenReturn(Category.CONNECTION);
        when(notExpected.getComTaskExecution()).thenReturn(interestedComTaskExecution);
        when(notExpected.getConnectionTask()).thenReturn(interestedConnectionTask);
        when(notExpected.getComPort()).thenReturn(otherComPort);

        // Business methods
        filteringEventReceiver.narrowToConnectionTasks(Arrays.asList(interestedConnectionTask));
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(interestedComTaskExecution));
        filteringEventReceiver.narrowToComPorts(Arrays.asList(interestedComPort));
        filteringEventReceiver.receive(notExpected);

        // Asserts
        verify(eventReceiver, never()).receive(notExpected);
    }

    @Test
    public void testNarrowToOtherComTasks () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(1L);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(1L);
        ComTaskExecution otherComTaskExecution = mock(ComTaskExecution.class);
        when(otherComTaskExecution.getId()).thenReturn(2L);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent notExpected = mock(ComTaskExecutionEvent.class);
        when(notExpected.getCategory()).thenReturn(Category.CONNECTION);
        when(notExpected.getComTaskExecution()).thenReturn(comTaskExecution);
        when(notExpected.getConnectionTask()).thenReturn(connectionTask);
        ComTaskExecutionEvent expected = mock(ComTaskExecutionEvent.class);
        when(expected.getCategory()).thenReturn(Category.CONNECTION);
        when(expected.getComTaskExecution()).thenReturn(otherComTaskExecution);
        when(expected.getConnectionTask()).thenReturn(connectionTask);

        // Business methods
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(comTaskExecution));
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(otherComTaskExecution));
        filteringEventReceiver.receive(notExpected);
        filteringEventReceiver.receive(expected);

        // Asserts
        verify(eventReceiver).receive(expected);
        verify(eventReceiver, never()).receive(notExpected);
    }

    @Test
    public void testWidenToAllComTasks () {
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTaskExecution otherComTaskExecution = mock(ComTaskExecution.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.getCategory()).thenReturn(Category.COMTASK);
        when(event.getComTaskExecution()).thenReturn(otherComTaskExecution);
        when(event.getConnectionTask()).thenReturn(connectionTask);

        // Business methods
        filteringEventReceiver.narrowToComTaskExecutions(Arrays.asList(comTaskExecution));
        filteringEventReceiver.widenToAllComTasks();
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToComPort () {
        ComPort comPort = mock(ComPort.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComPortRelatedEvent event = mock(ComPortRelatedEvent.class);
        when(event.isComPortRelated()).thenReturn(true);
        when(event.getComPort()).thenReturn(comPort);

        // Business methods
        filteringEventReceiver.narrowToComPorts(Arrays.asList(comPort));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToOtherComPorts () {
        ComPort interestedComPort = mock(ComPort.class);
        ComPort otherComPort = mock(ComPort.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComPortRelatedEvent event = mock(ComPortRelatedEvent.class);
        when(event.isComPortRelated()).thenReturn(true);
        when(event.getComPort()).thenReturn(otherComPort);

        // Business methods
        filteringEventReceiver.narrowToComPorts(Arrays.asList(interestedComPort));
        filteringEventReceiver.narrowToComPorts(Arrays.asList(otherComPort));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToComPortWithEventFromOtherComPort () {
        ComPort interestedComPort = mock(ComPort.class);
        when(interestedComPort.getId()).thenReturn(546l);
        ComPort otherComPort = mock(ComPort.class);
        when(otherComPort.getId()).thenReturn(1l);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComPortRelatedEvent event = mock(ComPortRelatedEvent.class);
        when(event.isComPortRelated()).thenReturn(true);
        when(event.getComPort()).thenReturn(otherComPort);

        // Business methods
        filteringEventReceiver.narrowToComPorts(Arrays.asList(interestedComPort));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testWidenToAllComPorts () {
        ComPort interestedComPort = mock(ComPort.class);
        ComPort otherComPort = mock(ComPort.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComPortRelatedEvent event = mock(ComPortRelatedEvent.class);
        when(event.isComPortRelated()).thenReturn(true);
        when(event.getComPort()).thenReturn(otherComPort);

        // Business methods
        filteringEventReceiver.narrowToComPorts(Arrays.asList(interestedComPort));
        filteringEventReceiver.widenToAllComPorts();
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToComPortPool () {
        ComPortPool comPortPool = mock(ComPortPool.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComPortPoolRelatedEvent event = mock(ComPortPoolRelatedEvent.class);
        when(event.isComPortPoolRelated()).thenReturn(true);
        when(event.getComPortPool()).thenReturn(comPortPool);

        // Business methods
        filteringEventReceiver.narrowToComPortPools(Arrays.asList(comPortPool));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToOtherComPortPools () {
        ComPortPool interestedComPortPool = mock(ComPortPool.class);
        ComPortPool otherComPortPool = mock(ComPortPool.class);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComPortPoolRelatedEvent event = mock(ComPortPoolRelatedEvent.class);
        when(event.isComPortPoolRelated()).thenReturn(true);
        when(event.getComPortPool()).thenReturn(otherComPortPool);

        // Business methods
        filteringEventReceiver.narrowToComPortPools(Arrays.asList(interestedComPortPool));
        filteringEventReceiver.narrowToComPortPools(Arrays.asList(otherComPortPool));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToComPortPoolWithEventsFromOtherComPortPool () {
        ComPortPool interestedComPortPool = mock(ComPortPool.class);
        when(interestedComPortPool.getId()).thenReturn(1L);
        ComPortPool otherComPortPool = mock(ComPortPool.class);
        when(otherComPortPool.getId()).thenReturn(2L);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComPortPoolRelatedEvent event = mock(ComPortPoolRelatedEvent.class);
        when(event.isComPortPoolRelated()).thenReturn(true);
        when(event.getComPortPool()).thenReturn(otherComPortPool);

        // Business methods
        filteringEventReceiver.narrowToComPortPools(Arrays.asList(interestedComPortPool));
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testWidenToAllComPortPools () {
        ComPortPool interestedComPortPool = mock(ComPortPool.class);
        when(interestedComPortPool.getId()).thenReturn(1L);
        ComPortPool otherComPortPool = mock(ComPortPool.class);
        when(otherComPortPool.getId()).thenReturn(2L);
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        ComPortPoolRelatedEvent event = mock(ComPortPoolRelatedEvent.class);
        when(event.isComPortPoolRelated()).thenReturn(true);
        when(event.getComPortPool()).thenReturn(otherComPortPool);

        // Business methods
        filteringEventReceiver.narrowToComPortPools(Arrays.asList(interestedComPortPool));
        filteringEventReceiver.widenToAllComPortPools();
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToLogLevel () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        LoggingEvent event = mock(LoggingEvent.class);
        when(event.isLoggingRelated()).thenReturn(true);
        when(event.getLogLevel()).thenReturn(LogLevel.INFO);

        // Business methods
        filteringEventReceiver.narrowToLogLevel(LogLevel.INFO);
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToOtherLogLevel () {
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        LoggingEvent event = mock(LoggingEvent.class);
        when(event.isLoggingRelated()).thenReturn(true);
        when(event.getLogLevel()).thenReturn(LogLevel.INFO);

        // Business methods
        filteringEventReceiver.narrowToLogLevel(LogLevel.DEBUG);
        filteringEventReceiver.narrowToLogLevel(LogLevel.INFO);
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

    @Test
    public void testNarrowToLogLevelWithEventsFromOtherLogLevel () {
        LogLevel interestedLogLevel = LogLevel.INFO;
        LogLevel otherLogLevel = LogLevel.DEBUG;
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        LoggingEvent event = mock(LoggingEvent.class);
        when(event.isLoggingRelated()).thenReturn(true);
        when(event.getLogLevel()).thenReturn(otherLogLevel);

        // Business methods
        filteringEventReceiver.narrowToLogLevel(interestedLogLevel);
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver, never()).receive(event);
    }

    @Test
    public void testWidenToAllLogLevels () {
        LogLevel interestedLogLevel = LogLevel.INFO;
        LogLevel otherLogLevel = LogLevel.DEBUG;
        EventReceiver eventReceiver = mock(EventReceiver.class);
        FilteringEventReceiver filteringEventReceiver = new FilteringEventReceiverImpl(eventReceiver);
        LoggingEvent event = mock(LoggingEvent.class);
        when(event.isLoggingRelated()).thenReturn(true);
        when(event.getLogLevel()).thenReturn(otherLogLevel);

        // Business methods
        filteringEventReceiver.narrowToLogLevel(interestedLogLevel);
        filteringEventReceiver.widenToAllLogLevels();
        filteringEventReceiver.receive(event);

        // Asserts
        verify(eventReceiver).receive(event);
    }

}