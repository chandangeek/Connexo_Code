/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.util.EnumSet;
import java.util.List;

/**
 * extends the {@link EventReceiver} interface to
 * filter {@link com.energyict.mdc.engine.events.ComServerEvent}s
 * according to specified criteria and will
 * finally forward the ComServerEvent to the
 * actual EventReceiver when the ComServerEvent
 * does not need to be filtered.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (17:39)
 */
public interface FilteringEventReceiver extends EventReceiver {

    /**
     * Tests if this FilteringEventReceiver delegates
     * {@link com.energyict.mdc.engine.events.ComServerEvent}s that are NOT filtered
     * to the specified {@link EventReceiver}.
     *
     * @param eventReceiver The EventReceiver;
     * @return <code>true</code> iff this FilteringEventReceiver delegates non-filtered ComServerEvents to the specified EventReceiver
     */
    boolean delegatesTo(EventReceiver eventReceiver);

    /**
     * Adds a filter such that only {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * of the specified categories are delegated
     * to the actual {@link EventReceiver}.
     *
     * @param wantedCategories The set of {@link Category wantedCategories} that need to be delegated
     */
    void narrowTo(EnumSet<Category> wantedCategories);

    /**
     * Adds a filter such that only {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to the specified {@link com.energyict.mdc.upl.meterdata.Device device}s are delegated
     * to the actual {@link EventReceiver}.
     *
     * @param devices The devices to which ComServerEvent should relate
     */
    void narrowToDevices(List<Device> devices);

    /**
     * Removes the filter for {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to any specific {@link com.energyict.mdc.upl.meterdata.Device device}.
     */
    void widenToAllDevices();

    /**
     * Adds a filter such that only {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to the specified {@link ConnectionTask}s are delegated
     * to the actual {@link EventReceiver}.
     *
     * @param connectionTasks The ConnectionTasks to which ComServerEvents should relate
     */
    void narrowToConnectionTasks(List<ConnectionTask> connectionTasks);

    /**
     * Removes the filter for {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to any specific {@link ConnectionTask}.
     */
    void widenToAllConnectionTasks();

    /**
     * Adds a filter such that only {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to the specified {@link ComTaskExecution}s are delegated
     * to the actual {@link EventReceiver}.
     *
     * @param comTaskExecutions The ComTaskExecution to which ComServerEvents should relate
     */
    void narrowToComTaskExecutions(List<ComTaskExecution> comTaskExecutions);

    /**
     * Removes the filter for {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to any specific {@link ComTask}.
     */
    void widenToAllComTasks();

    /**
     * Adds a filter such that only {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to the specified {@link ComPort}s are delegated
     * to the actual {@link EventReceiver}.
     *
     * @param comPorts The ComPorts to which ComServerEvents should relate
     */
    void narrowToComPorts(List<ComPort> comPorts);

    /**
     * Removes the filter for {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to any specific {@link ComPort}.
     */
    void widenToAllComPorts();

    /**
     * Adds a filter such that only {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to the specified ComPortPools are delegated
     * to the actual {@link EventReceiver}.
     *
     * @param comPortPools The ComPortPools to which ComServerEvents should relate
     */
    void narrowToComPortPools(List<ComPortPool> comPortPools);

    /**
     * Removes the filter for {@link com.energyict.mdc.engine.events.ComServerEvent}s
     * that relate to any specific ComPortPool.
     */
    void widenToAllComPortPools();

    /**
     * Adds a filter such that only {@link com.energyict.mdc.engine.events.LoggingEvent}s
     * that are emitted at the specified {@link LogLevel}s (or lower)
     * are delegated to the actual {@link EventReceiver}.
     *
     * @param logLevel The LogLevel
     */
    void narrowToLogLevel(LogLevel logLevel);

    /**
     * Removes the filter for {@link com.energyict.mdc.engine.events.LoggingEvent}s.
     */
    void widenToAllLogLevels();

}