package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.TopologyAction;

import com.energyict.cbo.LastSeenDateInfo;

import java.util.List;
import java.util.Map;

/**
 * Identifies the topology of a Device.
 */
public interface CollectedTopology extends CollectedData {

    /**
     * @return the unique identifier of the Device
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * @return a map containing the unique device identifiers of all attached slave devices, mapped to the proper LastSeenDate info for each slave device.
     * Note that protocols don't have to provide the LastSeenDate info, it can be null.
     * <p/>
     * The ComServer framework will take the provided LastSeenDate info into account when updating the GW link of the slaves.
     * <p/>
     * If this device has no attached slaves, the return list is empty.
     */
    Map<DeviceIdentifier, LastSeenDateInfo> getSlaveDeviceIdentifiers();

    /**
     * Add a slave device to the topology, without any information on its "last seen date".
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    void addSlaveDevice(DeviceIdentifier slaveIdentifier);

    /**
     * Add a slave device to the topology
     *
     * @param slaveIdentifier  the device identifier of the slave device
     * @param lastSeenDateInfo information on when this slave device was last seen by the gateway/DC.
     */
    void addSlaveDevice(DeviceIdentifier slaveIdentifier, LastSeenDateInfo lastSeenDateInfo);

    /**
     * Remove a slave device from the topology
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    void removeSlaveDevice(DeviceIdentifier slaveIdentifier);

    /**
     * @return a list containing additional {@link CollectedDeviceInfo} collected data,
     * this could be an ip address / protocol property / ... for a device
     */
    List<CollectedDeviceInfo> getAdditionalCollectedDeviceInfo();

    /**
     * Add a new {@link CollectedDeviceInfo} collected data to the collected topology
     */
    void addAdditionalCollectedDeviceInfo(CollectedDeviceInfo additionalDeviceInfo);

    /**
     * Getter for the {@link TopologyAction}
     */
    TopologyAction getTopologyAction();

    /**
     * Setter for the {@link TopologyAction}
     *
     * @param topologyAction The {@link TopologyAction}
     */
    void setTopologyAction(TopologyAction topologyAction);

}