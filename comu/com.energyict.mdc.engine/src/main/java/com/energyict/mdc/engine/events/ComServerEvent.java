package com.energyict.mdc.engine.events;

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
    public Instant getOccurrenceTimestamp ();

    /**
     * Gets the {@link Category} to which this event belongs.
     *
     * @return The Category
     */
    public Category getCategory ();

    /**
     * Tests if this ComServerEvent relates to a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link DeviceRelatedEvent} to obtain the device
     * from the event. The device that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a device
     */
    public boolean isDeviceRelated ();

    /**
     * Tests if this ComServerEvent relates to a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link ConnectionTaskRelatedEvent} to obtain the ConnectionTask
     * from the event. The ConnectionTask that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a ConnectionTask
     */
    public boolean isConnectionTaskRelated ();

    /**
     * Tests if this ComServerEvent relates to a {@link com.energyict.mdc.engine.config.ComPort}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link ComPortRelatedEvent} to obtain the ComPort
     * from the event. The ComPort that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a ComPort
     */
    public boolean isComPortRelated ();

    /**
     * Tests if this ComServerEvent relates to a {@link com.energyict.mdc.engine.config.ComPortPool}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link ComPortPoolRelatedEvent} to obtain the ComPortPool
     * from the event. The ComPortPool that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a ComPortPool
     */
    public boolean isComPortPoolRelated ();

    /**
     * Tests if this ComServerEvent relates to a {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link ComTaskExecutionRelatedEvent} to obtain the ComTaskExecution
     * from the event. The ComTaskExecution that is obtained from the event is guaranteed
     * not be be <code>null</code>.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a ComTaskExecution
     */
    public boolean isComTaskExecutionRelated ();

    /**
     * Tests if this ComServerEvent relates to log messages
     * being produced (or emitted) by the ComServer.
     * When that is the case, it should be possible to cast
     * this ComServerEvent to {@link LoggingEvent} to obtain
     * information of the emitted log message.
     *
     * @return <code>true</code> iff this ComServerEvent relates to a log message
     */
    public boolean isLoggingRelated ();

}