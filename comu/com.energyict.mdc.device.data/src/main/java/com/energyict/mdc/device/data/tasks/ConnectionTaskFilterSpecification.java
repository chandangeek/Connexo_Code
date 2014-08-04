package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Models the filtering that can be applied by client code to count
 * {@link  com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * by their respective {@link TaskStatus}.
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
    public Set<TaskStatus> taskStatuses = EnumSet.allOf(TaskStatus.class);

    /**
     * The flag that indicates if only the data from the {@link ConnectionTask}'s
     * last communication session should be used.
     */
    public boolean useLastComSession = true;

}