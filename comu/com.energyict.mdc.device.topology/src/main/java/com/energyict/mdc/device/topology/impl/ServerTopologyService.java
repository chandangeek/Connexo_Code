/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.topology.PhysicalGatewayReference;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Adds behavior to {@link TopologyService} that is specific
 * to server side components.
 */
public interface ServerTopologyService extends TopologyService {

    DataModel dataModel();

    /**
     * Sets or updates the default {@link ConnectionTask} on
     * {@link ComTaskExecution}s
     * that relate to all {@link Device}s in the topology that starts
     * at the specified master Device.
     *
     * @param device The master device
     * @param connectionTask The new default ConnectionTask
     */
    void setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology(Device device, ConnectionTask connectionTask);

    /**
     * Sets or updates the {@link ConnectionTask}, which has a {@link ConnectionFunction} defined, on
     * {@link ComTaskExecution}s that relate to all {@link Device}s in the topology that starts
     * at the specified master Device. <br/>
     * This method should be used when the connection task starts to use a certain connection function. By using this method,
     * this change will be propagated to the necessary ComTaskExecutions.
     *
     * @param device The master device
     * @param connectionTask The new ConnectionTask having the {@link ConnectionFunction} defined
     */
    void setOrUpdateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(Device device, ConnectionTask connectionTask);

    /**
     * Clears the {@link ConnectionTask} from all {@link ComTaskExecution}s that have the given
     * {@link ConnectionFunction} set and that relate to all {@link Device}s in the topology that starts
     * at the specified master Device. <br/>
     * This method should be used when the connection task no longer uses a certain connection function. By using this method,
     * this change will be propagated to the necessary ComTaskExecutions.
     *
     *
     * @param device The master device
     * @param connectionFunction The {@link ConnectionFunction} to search for
     */
    void clearConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(Device device, ConnectionFunction connectionFunction);

    Optional<PhysicalGatewayReference> getPhysicalGatewayReference(Device slave, Instant when);

    List<PhysicalGatewayReference> getPhysicalGateWayReferencesFrom(Device slave, Instant when);

    void terminateTemporal(PhysicalGatewayReference gatewayReference, Instant now);

    void slaveTopologyChanged(Device slave, Optional<Device> gateway);

}