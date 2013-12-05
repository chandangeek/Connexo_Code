package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;

import java.util.List;

/**
 * Identifies the topology of a Device.
 */
public interface CollectedTopology extends CollectedData {

    /**
     * @return the unique identifier of the Device
     */
    public DeviceIdentifier getDeviceIdentifier();

    /**
     * @return a list containing the unique device identifiers of all attached slave devices
     *         If this device has no attached slaves, the return list is empty.
     */
    public List<DeviceIdentifier> getSlaveDeviceIdentifiers();

    /**
     * Add a slave device to the topology
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    public void addSlaveDevice(DeviceIdentifier slaveIdentifier);

    /**
     * Remove a slave device from the topology
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    public void removeSlaveDevice(DeviceIdentifier slaveIdentifier);

    /**
     * Getter for the {@link TopologyAction}
     */
    public TopologyAction getTopologyAction();

    /**
     * Setter for the {@link TopologyAction}
     * @param topologyAction
     */
    public void setTopologyAction(TopologyAction topologyAction);

    public void setDataCollectionConfiguration (DataCollectionConfiguration configuration);

}