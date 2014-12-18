package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.orm.DataModel;

/**
 * Adds behavior to {@link TopologyService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:23)
 */
public interface ServerTopologyService extends TopologyService {

    public DataModel dataModel();

    /**
     * Sets or updates the default {@link ConnectionTask} on
     * {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
     * that relate to all {@link Device}s in the topology that starts
     * at the specified master Device.
     *
     * @param device The master device
     * @param connectionTask The new default ConnectionTask
     */
    public void setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology(Device device, ConnectionTask connectionTask);

}