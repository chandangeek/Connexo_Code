/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.Device;
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
     * {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
     * that relate to all {@link Device}s in the topology that starts
     * at the specified master Device.
     *
     * @param device The master device
     * @param connectionTask The new default ConnectionTask
     */
    void setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology(Device device, ConnectionTask connectionTask);

    /**
     * Sets or updates the {@link ConnectionTask}, which has a {@link ConnectionFunction} defined, on
     * {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
     * that relate to all {@link Device}s in the topology that starts
     * at the specified master Device.
     *
     * @param device The master device
     * @param connectionTask The new ConnectionTask having the {@link ConnectionFunction} defined
     */
    void setOrUpdateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(Device device, ConnectionTask connectionTask);

    Optional<PhysicalGatewayReference> getPhysicalGatewayReference(Device slave, Instant when);

    List<PhysicalGatewayReference> getPhysicalGateWayReferencesFrom(Device slave, Instant when);

    void terminateTemporal(PhysicalGatewayReference gatewayReference, Instant now);

    void slaveTopologyChanged(Device slave, Optional<Device> gateway);

}