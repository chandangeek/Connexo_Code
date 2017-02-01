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
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.util.List;
import java.util.Set;

/**
 * Publishes {@link ComServerEvent}s to {@link EventReceiver}s that have registered.
 * An EventReceiver can register a general interest and will
 * receive all events. It can also register an interest in some
 * {@link Category event categories}.
 * These interests can later be fine tuned to receive
 * only events that relate to e.g. a single {@link com.energyict.mdc.protocol.api.device.BaseDevice device},
 * a single {@link ConnectionTask} or a single (@link ComTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (09:08)
 */
public interface EventPublisher {

    /**
     * Registers the {@link EventReceiver} as being interested
     * to receive <strong>ALL</strong> {@link ComServerEvent}s.
     *
     * @param receiver The EventReceiver
     */
    void registerInterest (EventReceiver receiver);

    /**
     * Narrows the registered interest of the {@link EventReceiver}
     * to {@link ComServerEvent}s of the specified {@link Category categories}.
     *
     * @param receiver The EventReceiver
     * @param categories The set of event categories in which the EventReceiver is interested
     */
    void narrowInterestToCategories (EventReceiver receiver, Set<Category> categories);

    /**
     * Narrows the registered interest of the {@link EventReceiver}
     * to events that relate to the specified {@link com.energyict.mdc.protocol.api.device.BaseDevice device}s.
     *
     * @param receiver The EventReceiver
     * @param devices The devices
     */
    void narrowInterestToDevices (EventReceiver receiver, List<Device> devices);

    /**
     * Widens the registered interest so that not only events
     * that relate to the specified {@link com.energyict.mdc.protocol.api.device.BaseDevice} are published
     * to the specified {@link EventReceiver}.
     *
     * @param receiver The EventReceiver
     */
    void widenInterestToAllDevices (EventReceiver receiver);

    /**
     * Narrows the registered interest of the {@link EventReceiver}
     * to events that relate to the specified {@link ConnectionTask}s.
     * Note that since a ConnectionTask relates to a single device
     * or a single device hierarchy (master/slave devices)
     * narrowing your interest to a ConnectionTask
     * automatically narrows your interest to events
     * that relate to the single device or the device hierarchy.
     * You can narrow your interest to another ConnectionTask
     * by simply calling this method again with another ConnectionTask.
     *
     * @param receiver The EventReceiver
     * @param connectionTask The ConnectionTask
     */
    void narrowInterestToConnectionTasks (EventReceiver receiver, List<ConnectionTask> connectionTask);

    /**
     * Widens the registered interest so that not only events
     * that relate to the specified {@link ConnectionTask} are published
     * to the specified {@link EventReceiver}.
     *
     * @param receiver The EventReceiver
     */
    void widenInterestToAllConnectionTasks (EventReceiver receiver);

    /**
     * Narrows the registered interest to events that relate
     * to the specified {@link ComTaskExecution}s.
     * Note that this can be combined narrowing your interest
     * to a {@link com.energyict.mdc.protocol.api.device.BaseDevice device} or a {@link ConnectionTask}.
     * You can narrow your interest to another ComTaskExecution
     * by simply calling this method again with another ComTaskExecution.
     *
     * @param receiver The EventReceiver
     * @param comTaskExecution The ComTaskExecution
     */
    void narrowInterestToComTaskExecutions (EventReceiver receiver, List<ComTaskExecution> comTaskExecution);

    /**
     * Widens the registered interest so that not only events
     * that relate to the specified {@link ComTaskExecution} are published
     * to the specified {@link EventReceiver}.
     *
     * @param receiver The EventReceiver
     */
    void widenInterestToAllComTaskExecutions (EventReceiver receiver);

    /**
     * Narrows the registered interest to events that relate
     * to the specified {@link ComPort}s. Note that this can be combined
     * narrowing your interest to a {@link com.energyict.mdc.protocol.api.device.BaseDevice device} or a {@link ConnectionTask}.
     * You can narrow your interest to another ComPort
     * by simply calling this method again with another ComPort.
     *
     * @param receiver The EventReceiver
     * @param comPorts The ComPorts
     */
    void narrowInterestToComPorts (EventReceiver receiver, List<ComPort> comPorts);

    /**
     * Widens the registered interest so that not only events
     * that relate to the specified {@link ComPort} are published
     * to the specified {@link EventReceiver}.
     *
     * @param receiver The EventReceiver
     */
    void widenInterestToAllComPorts (EventReceiver receiver);

    /**
     * Narrows the registered interest to events that relate
     * to the specified ComPortPools. Note that this can be combined
     * narrowing your interest to a Device or a {@link ConnectionTask}.
     * You can narrow your interest to another ComPortPool
     * by simply calling this method again with another ComPortPool.
     *
     * @param receiver The EventReceiver
     * @param comPortPool The ComPortPool
     */
    void narrowInterestToComPortPools (EventReceiver receiver, List<ComPortPool> comPortPool);

    /**
     * Widens the registered interest so that not only events
     * that relate to the specified ComPortPool are published
     * to the specified {@link EventReceiver}.
     *
     * @param receiver The EventReceiver
     */
    void widenInterestToAllComPortPools (EventReceiver receiver);

    /**
     * Narrows the registered interest to {@link com.energyict.mdc.engine.events.LoggingEvent}s
     * that were emitted at the specified {@link LogLevel} or a level below.
     * Note that if the EventReceiver registers an interest for
     * the highest LogLevel, the EventReceiver will receive
     * all possible LoggingEvents and that is in fact
     * equal to calling {@link #widenToAllLogLevels(EventReceiver)}.
     *
     * @param eventReceiver The EventReceiver
     * @param logLevel The LogLevel of interest
     */
    void narrowInterestToLogLevel (EventReceiver eventReceiver, LogLevel logLevel);

    /**
     * Unregisters the {@link EventReceiver}'s interest in {@link com.energyict.mdc.engine.events.LoggingEvent}s.
     *
     * @param eventReceiver The EventReceiver
     */
    void widenToAllLogLevels (EventReceiver eventReceiver);

    /**
     * Unregisters all interests previously indicated by the {@link EventReceiver}.
     * The EventReceiver will no longer receive any events,
     * even if those would match interests that were registered before.
     *
     * @param receiver The EventReceiver
     */
    void unregisterAllInterests (EventReceiver receiver);

    /**
     * Publishes the specified {@link ComServerEvent}
     * to all interested/registered parties.
     *
     * @param event The ComServerEvent
     */
    void publish (ComServerEvent event);

    void shutdown();

    default void answerPing(){
        // do nothing;
    }

}