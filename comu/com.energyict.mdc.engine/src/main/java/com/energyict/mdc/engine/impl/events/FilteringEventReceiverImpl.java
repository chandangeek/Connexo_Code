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
import com.energyict.mdc.engine.impl.events.filtering.CategoryFilter;
import com.energyict.mdc.engine.impl.events.filtering.ComPortFilter;
import com.energyict.mdc.engine.impl.events.filtering.ComPortPoolFilter;
import com.energyict.mdc.engine.impl.events.filtering.ComTaskExecutionFilter;
import com.energyict.mdc.engine.impl.events.filtering.ConnectionTaskFilter;
import com.energyict.mdc.engine.impl.events.filtering.DeviceFilter;
import com.energyict.mdc.engine.impl.events.filtering.EventFilterCriterion;
import com.energyict.mdc.engine.impl.events.filtering.LogLevelFilter;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link FilteringEventReceiver} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (10:17)
 */
public class FilteringEventReceiverImpl implements FilteringEventReceiver {

    private EventReceiver actualReceiver;
    private List<EventFilterCriterion> criteria = new LinkedList<>();

    public FilteringEventReceiverImpl (EventReceiver actualReceiver) {
        super();
        this.actualReceiver = actualReceiver;
    }

    @Override
    public boolean delegatesTo (EventReceiver eventReceiver) {
        return this.actualReceiver.equals(eventReceiver);
    }

    @Override
    public void receive (ComServerEvent event) {
        synchronized (this.criteria) {
            for (EventFilterCriterion criterion : this.criteria) {
                if (criterion.matches(event)) {
                    // A criterion matches so filter this event
                    return;
                }
            }
        }
        // None of the filter criteria matches so no filtering applies and we forward to the actual EventReceiver
        this.actualReceiver.receive(event);
    }

    @Override
    public void narrowTo (EnumSet<Category> wantedCategories) {
        Set<Category> filteredCategories = EnumSet.complementOf(wantedCategories);
        synchronized (this.criteria) {
            for (EventFilterCriterion criterion : this.criteria) {
                if (criterion instanceof CategoryFilter) {
                    CategoryFilter categoryFilter = (CategoryFilter) criterion;
                    categoryFilter.setCategories(filteredCategories);
                    return;
                }
            }
            this.criteria.add(new CategoryFilter(filteredCategories));
        }
    }

    @Override
    public void narrowToDevices (List<Device> device) {
        synchronized (this.criteria) {
            for (EventFilterCriterion criterion : this.criteria) {
                if (criterion instanceof DeviceFilter) {
                    DeviceFilter deviceFilter = (DeviceFilter) criterion;
                    deviceFilter.setDevices(device);
                    return;
                }
            }
            this.criteria.add(new DeviceFilter(device));
        }
    }

    @Override
    public void widenToAllDevices () {
        this.removeFilterOfType(DeviceFilter.class);
    }

    @Override
    public void narrowToConnectionTasks (List<ConnectionTask> connectionTasks) {
        synchronized (this.criteria) {
            this.narrowToDevices(this.collectDevicesFrom(connectionTasks));
            for (EventFilterCriterion criterion : this.criteria) {
                if (criterion instanceof ConnectionTaskFilter) {
                    ConnectionTaskFilter connectionTaskFilter = (ConnectionTaskFilter) criterion;
                    connectionTaskFilter.setConnectionTasks(connectionTasks);
                    return;
                }
            }
            this.criteria.add(new ConnectionTaskFilter(connectionTasks));
        }
    }

    private List<Device> collectDevicesFrom (List<ConnectionTask> connectionTasks) {
        return connectionTasks.stream().map(ConnectionTask::getDevice).collect(Collectors.toList());
    }

    @Override
    public void widenToAllConnectionTasks () {
        this.removeFilterOfType(ConnectionTaskFilter.class);
    }

    @Override
    public void narrowToComTaskExecutions (List<ComTaskExecution> comTaskExecutions) {
        synchronized (this.criteria) {
            for (EventFilterCriterion criterion : this.criteria) {
                if (criterion instanceof ComTaskExecutionFilter) {
                    ComTaskExecutionFilter filter = (ComTaskExecutionFilter) criterion;
                    filter.setComTaskExecutions(comTaskExecutions);
                    return;
                }
            }
            this.criteria.add(new ComTaskExecutionFilter(comTaskExecutions));
        }
    }

    @Override
    public void widenToAllComTasks () {
        this.removeFilterOfType(ComTaskExecutionFilter.class);
    }

    @Override
    public void narrowToComPorts (List<ComPort> comPorts) {
        synchronized (this.criteria) {
            for (EventFilterCriterion criterion : this.criteria) {
                if (criterion instanceof ComPortFilter) {
                    ComPortFilter comPortFilter = (ComPortFilter) criterion;
                    comPortFilter.setComPorts(comPorts);
                    return;
                }
            }
            this.criteria.add(new ComPortFilter(comPorts));
        }
    }

    @Override
    public void widenToAllComPorts () {
        this.removeFilterOfType(ComPortFilter.class);
    }

    @Override
    public void narrowToComPortPools (List<ComPortPool> comPortPools) {
        synchronized (this.criteria) {
            for (EventFilterCriterion criterion : this.criteria) {
                if (criterion instanceof ComPortPoolFilter) {
                    ComPortPoolFilter comPortPoolFilter = (ComPortPoolFilter) criterion;
                    comPortPoolFilter.setComPortPools(comPortPools);
                    return;
                }
            }
            this.criteria.add(new ComPortPoolFilter(comPortPools));
        }
    }

    @Override
    public void widenToAllComPortPools () {
        this.removeFilterOfType(ComPortPoolFilter.class);
    }

    @Override
    public void narrowToLogLevel (LogLevel logLevel) {
        synchronized (this.criteria) {
            for (EventFilterCriterion criterion : this.criteria) {
                if (criterion instanceof LogLevelFilter) {
                    LogLevelFilter logLevelFilter = (LogLevelFilter) criterion;
                    logLevelFilter.setLogLevel(logLevel);
                    return;
                }
            }
            this.criteria.add(new LogLevelFilter(logLevel));
        }
    }

    @Override
    public void widenToAllLogLevels () {
        this.removeFilterOfType(LogLevelFilter.class);
    }

    private <T extends EventFilterCriterion> void removeFilterOfType (Class<T> filterType) {
        synchronized (this.criteria) {
            Iterator<EventFilterCriterion> criterionIterator = this.criteria.iterator();
            while (criterionIterator.hasNext()) {
                EventFilterCriterion criterion = criterionIterator.next();
                if (filterType.isAssignableFrom(criterion.getClass())) {
                    criterionIterator.remove();
                }
            }
        }
    }

}