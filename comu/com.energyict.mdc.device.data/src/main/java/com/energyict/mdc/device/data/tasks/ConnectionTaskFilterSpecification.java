/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Models the filtering that can be applied by client code to count
 * or find {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * by a number of criteria that can be mixed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (16:55)
 */
public class ConnectionTaskFilterSpecification {

    /**
     * The Set of {@link ConnectionTypePluggableClass} or an empty set
     * if you want all ConnectionTypePluggableClasses that are configured in the system.
     */
    public Set<ConnectionTypePluggableClass> connectionTypes = new HashSet<>();

    /**
     * The Set of {@link ComPortPool} or an empty set
     * if you want all ComPortPools that are configured in the system.
     */
    public Set<ComPortPool> comPortPools = new HashSet<>();

    /**
     * The Set of {@link DeviceType} or an empty set
     * if you want all DeviceType that are configured in the system.
     */
    public Set<DeviceType> deviceTypes = new HashSet<>();

    /**
     * The Set of {@link TaskStatus}es.
     */
    public Set<TaskStatus> taskStatuses = EnumSet.noneOf(TaskStatus.class);

    /**
     * The Set of {@link ConnectionTask.SuccessIndicator}s.
     * Note that using <strong>ALL</strong> enum values is equal to using none.
     */
    public Set<ConnectionTask.SuccessIndicator> latestStatuses = EnumSet.noneOf(ConnectionTask.SuccessIndicator.class);

    /**
     * The Set of {@link ComSession.SuccessIndicator}s.
     * Note that using <strong>ALL</strong> enum values is equal to using none.
     */
    public Set<ComSession.SuccessIndicator> latestResults = EnumSet.noneOf(ComSession.SuccessIndicator.class);

    /**
     * The Interval in which the start time of the last session is expected
     * or <code>null</code> if the counter of the filter should not
     * take this into account.
     */
    public Interval lastSessionStart = null;

    /**
     * The Interval in which the end time of the last session is expected
     * or <code>null</code> if the counter of the filter should not
     * take this into account.
     */
    public Interval lastSessionEnd = null;

    /**
     * The flag that indicates if only the data from the {@link ConnectionTask}'s
     * last communication session should be used.
     */
    public boolean useLastComSession = false;

    /**
     * The {@link EndDeviceGroup}s that contain the {@link com.energyict.mdc.device.data.Device}s
     * to which the matching {@link ConnectionTask}s should be linked
     * or an empty set if the filter should not take this into account.
     */
    public Set<EndDeviceGroup> deviceGroups = new HashSet<>();

    /**
     * The Set of device states
     * Connection tasks of devices in such states will be excluded from the result
     * Default value: exclude connection tasks of "In stock" and "Decommissioned" devices
     */
    public Set<String> restrictedDeviceStates = new HashSet<>(Arrays.asList(DefaultState.IN_STOCK.getKey(), DefaultState.DECOMMISSIONED.getKey()));

    /**
     * The device(name) you want to filter on
     */
    public String deviceName;
}