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
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Provides a singleton implementation for the {@link EventPublisher} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (10:54)
 */
public class EventPublisherImpl implements EventPublisher {

    private final RunningComServer comServer;
    private FilteringEventReceiverFactory factory;
    private List<FilteringEventReceiver> filters = new LinkedList<>();

    public EventPublisherImpl(RunningComServer comServer) {
        this(comServer, new FilteringEventReceiverFactoryImpl());
    }

    public EventPublisherImpl(RunningComServer comServer, FilteringEventReceiverFactory factory) {
        super();
        this.comServer = comServer;
        this.factory = factory;
    }

    @Override
    public void shutdown() {
        // Nothing to shutdown
    }

    @Override
    public void unregisterAllInterests(EventReceiver receiver) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findFilter(receiver);
            if (filter != null) {
                this.filters.remove(filter);
                this.notifyClientUnregistered();
            }
        }
    }

    @Override
    public void registerInterest(EventReceiver receiver) {
        this.narrowInterestToCategories(receiver, EnumSet.allOf(Category.class));
    }

    @Override
    public void narrowInterestToCategories(EventReceiver receiver, Set<Category> categories) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.narrowTo(EnumSet.copyOf(categories));
        }
    }

    @Override
    public void narrowInterestToDevices(EventReceiver receiver, List<Device> devices) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.narrowToDevices(devices);
        }
    }

    @Override
    public void widenInterestToAllDevices(EventReceiver receiver) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.widenToAllDevices();
        }
    }

    @Override
    public void narrowInterestToConnectionTasks(EventReceiver receiver, List<ConnectionTask> connectionTasks) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.narrowToConnectionTasks(connectionTasks);
        }
    }

    @Override
    public void widenInterestToAllConnectionTasks(EventReceiver receiver) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.widenToAllConnectionTasks();
        }
    }

    @Override
    public void narrowInterestToComTaskExecutions(EventReceiver receiver, List<ComTaskExecution> comTaskExecutions) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.narrowToComTaskExecutions(comTaskExecutions);
        }
    }

    @Override
    public void widenInterestToAllComTaskExecutions(EventReceiver receiver) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.widenToAllComTasks();
        }
    }

    @Override
    public void narrowInterestToComPorts(EventReceiver receiver, List<ComPort> comPorts) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.narrowToComPorts(comPorts);
        }
    }

    @Override
    public void widenInterestToAllComPorts(EventReceiver receiver) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.widenToAllComPorts();
        }
    }

    @Override
    public void narrowInterestToComPortPools(EventReceiver receiver, List<ComPortPool> comPortPools) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.narrowToComPortPools(comPortPools);
        }
    }

    @Override
    public void widenInterestToAllComPortPools(EventReceiver receiver) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.widenToAllComPortPools();
        }
    }

    @Override
    public void narrowInterestToLogLevel(EventReceiver receiver, LogLevel logLevel) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.narrowToLogLevel(logLevel);
        }
    }

    @Override
    public void widenToAllLogLevels(EventReceiver receiver) {
        synchronized (this.filters) {
            FilteringEventReceiver filter = this.findOrCreateFilter(receiver);
            filter.widenToAllLogLevels();
        }
    }

    private FilteringEventReceiver findOrCreateFilter(EventReceiver receiver) {
        FilteringEventReceiver filter = this.findFilter(receiver);
        if (filter == null) {
            filter = this.createFilter(receiver);
        }
        return filter;
    }

    private FilteringEventReceiver createFilter(EventReceiver receiver) {
        FilteringEventReceiver filter = this.factory.newFor(receiver);
        this.filters.add(filter);
        this.notifyClientRegistered();
        return filter;
    }

    private void notifyClientRegistered() {
        this.comServer.eventClientRegistered();
    }

    private void notifyClientUnregistered() {
        this.comServer.eventClientUnregistered();
    }

    private void notifyEventWasPublished() {
        this.comServer.eventWasPublished();
    }

    private FilteringEventReceiver findFilter(EventReceiver receiver) {
        for (FilteringEventReceiver filter : this.filters) {
            if (filter.delegatesTo(receiver)) {
                return filter;
            }
        }
        return null;
    }

    @Override
    public void publish(ComServerEvent event) {
        synchronized (this.filters) {
            for (FilteringEventReceiver filter : this.filters) {
                filter.receive(event);
            }
            this.notifyEventWasPublished();
        }
    }

}