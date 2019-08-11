/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.events;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models an event as in something that happened or took place
 * within the ComServer.
 * There are different {@link Category categories} of events.
 * Client application can register interests for events
 * and then receive those interest as and when they occur.
 * Some events will relate to one or more business objects.
 * Test methods are provided for each type of business object.
 * Also when a ComServerEvent returns <code>true</code> for
 * one of these test methods, it is guaranted that it should
 * be possible to cast that instance to the related XXXRelatedEvent class
 * to obtain that business object. As an example:
 * when {@link #isDeviceRelated()} returns <code>true</code> then
 * the ComServerEvent can be cast to {@link DeviceRelatedEvent}
 * and calling {@link DeviceRelatedEvent#getDevice()} will NOT return <code>null</code>.
 * <pre><code>
 *     ComServerEvent event = ...
 *     if (event.isDeviceRelated()) {
 *         DeviceRelatedEvent deviceEvent = (DeviceRelatedEvent) event;
 *         Device device = deviceEvent.getDevice();
 *         System.out.println(device.getName());
 *     }
 * </code></pre>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-30 (17:01)
 */
@ProviderType
public interface ComServerEvent {

    /**
     * Gets the timestamp on which this event occurred.
     *
     * @return The timestamp on which this event occurred
     */
    Instant getOccurrenceTimestamp();

    /**
     * Gets the {@link Category} to which this event belongs.
     *
     * @return The Category
     */
    Category getCategory();

    /**
     * Tests if this ComServerEvent relates to a {@link com.energyict.mdc.upl.meterdata.Device device}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link DeviceRelatedEvent} to obtain the device
     * from the event. The device that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a device
     */
    boolean isDeviceRelated();

    /**
     * Tests if this ComServerEvent relates to a {@link ConnectionTask}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link ConnectionTaskRelatedEvent} to obtain the ConnectionTask
     * from the event. The ConnectionTask that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a ConnectionTask
     */
    boolean isConnectionTaskRelated();

    /**
     * Tests if this ComServerEvent relates to a {@link ComPort}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link ComPortRelatedEvent} to obtain the ComPort
     * from the event. The ComPort that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a ComPort
     */
    boolean isComPortRelated();

    /**
     * Tests if this ComServerEvent relates to a {@link ComPortPool}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link ComPortPoolRelatedEvent} to obtain the ComPortPool
     * from the event. The ComPortPool that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a ComPortPool
     */
    boolean isComPortPoolRelated();

    /**
     * Tests if this ComServerEvent relates to a {@link ComTaskExecution}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link ComTaskExecutionRelatedEvent} to obtain the ComTaskExecution
     * from the event. The ComTaskExecution that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a ComTaskExecution
     */
    boolean isComTaskExecutionRelated();

    /**
     * Tests if this ComServerEvent relates to log messages
     * being produced (or emitted) by the ComServer.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link LoggingEvent} to obtain
     * information of the emitted log message.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a log message
     */
    boolean isLoggingRelated();

}