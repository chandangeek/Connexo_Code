package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.time.Duration;
import java.util.List;

/**
 * Identifies the topology of a Device.
 */
public interface CollectedTopology extends CollectedData {

    /**
     * @return the unique identifier of the Device
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * @return a list containing the unique device identifiers of all attached slave devices
     *         If this device has no attached slaves, the return list is empty.
     */
    List<DeviceIdentifier> getSlaveDeviceIdentifiers();

    /**
     * Add a slave device to the topology
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    void addSlaveDevice(DeviceIdentifier slaveIdentifier);

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
     * @param topologyAction
     */
    void setTopologyAction(TopologyAction topologyAction);

    void setDataCollectionConfiguration(DataCollectionConfiguration configuration);

    void addPathSegmentFor(DeviceIdentifier source, DeviceIdentifier target, DeviceIdentifier intermediateHop, Duration timeToLive, int cost);

    void addTopologyNeighbour(DeviceIdentifier neighbour, int modulationSchema, long toneMap, int modulation, int txGain, int txRes, int txCoeff, int lqi, int phaseDifferential, int tmrValidTime, int neighbourValidTime);

    void addG3IdentificationInformation(String formattedIPv6Address, int ipv6ShortAddress, int logicalDeviceId);

    List<TopologyPathSegment> getTopologyPathSegments();

    List<TopologyNeighbour> getTopologyNeighbours();

    G3TopologyDeviceAddressInformation getG3TopologyDeviceAddressInformation();
}