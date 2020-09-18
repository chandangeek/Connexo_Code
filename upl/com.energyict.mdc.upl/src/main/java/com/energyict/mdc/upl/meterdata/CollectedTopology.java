package com.energyict.mdc.upl.meterdata;

import com.energyict.cbo.ObservationDateProperty;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.TopologyAction;

import java.time.Duration;
import java.util.Date;
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
    Map<DeviceIdentifier, ObservationDateProperty> getSlaveDeviceIdentifiers();

    /**
     * @return the list of slave devices which joined the network.
     */
    Map<DeviceIdentifier, ObservationDateProperty> getJoinedSlaveDeviceIdentifiers();

    /**
     * @return the list of lost device identifiers.
     */
    List<DeviceIdentifier> getLostSlaveDeviceIdentifiers();

    /**
     * Add a slave device to the topology, without any information on its "last seen date".
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    void addSlaveDevice(DeviceIdentifier slaveIdentifier);

    /**
     * Adds a joined slave device to the topology. If a list of joined devices is present, only those will be processed.
     */
    void addJoinedSlaveDevice(DeviceIdentifier slaveIdentifier, ObservationDateProperty lastSeenDateInfo);

    /**
     * Adds a lost slave device to the topology. If a list of lost devices is present, only those will be processed.
     */
    void addLostSlaveDevice(DeviceIdentifier slaveIdentifier);


    /**
     * Add a slave device to the topology
     *
     * @param slaveIdentifier              the device identifier of the slave device
     * @param observationTimestampProperty information on when this slave device was last seen by the gateway/DC.
     */
    void addSlaveDevice(DeviceIdentifier slaveIdentifier, ObservationDateProperty observationTimestampProperty);

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

    void addPathSegmentFor(DeviceIdentifier source, DeviceIdentifier target, DeviceIdentifier intermediateHop, Duration timeToLive, int cost);

    void addTopologyNeighbour(DeviceIdentifier neighbour, int modulationSchema, long toneMap, int modulation,
                              int txGain, int txRes, int txCoeff, int lqi, int phaseDifferential, int tmrValidTime,
                              int neighbourValidTime, long macPANId, String nodeAddress, int shortAddress, Date lastUpdate,
                              Date lastPathRequest, int state, long roundTrip, int linkCost);

    void addG3IdentificationInformation(String formattedIPv6Address, int ipv6ShortAddress, int logicalDeviceId);

    List<TopologyPathSegment> getTopologyPathSegments();

    List<TopologyNeighbour> getTopologyNeighbours();

    G3TopologyDeviceAddressInformation getG3TopologyDeviceAddressInformation();

}