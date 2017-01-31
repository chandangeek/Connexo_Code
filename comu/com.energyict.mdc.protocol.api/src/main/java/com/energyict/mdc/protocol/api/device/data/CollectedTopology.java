/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;

import java.time.Duration;
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
     * If this device has no attached slaves, the returned list is empty.
     */
    public List<DeviceIdentifier> getSlaveDeviceIdentifiers();

//    /**
//     * TODO -> NOT really required for 10.2, saves a major bump
//     * @return a list containing the unique device identifiers and lastSeenDate info for all attached slave devices
//     * If this device has no attached slaves, the returned map will be empty.
//     */
//    public Map<DeviceIdentifier, LastSeenDateInfo> getSlaveDeviceIdentifiersWithLastSeenDate();

    /**
     * Add a slave device to the topology
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    public void addSlaveDevice(DeviceIdentifier slaveIdentifier);

//    /**
//     * TODO -> NOT really required for 10.2, saves a major bump
//     * Add a slave device to the topology
//     *
//     * @param slaveIdentifier  the device identifier of the slave device
//     * @param lastSeenDateInfo information on when this slave device was last seen by the gateway/DC.
//     */
//    public void addSlaveDevice(DeviceIdentifier slaveIdentifier, LastSeenDateInfo lastSeenDateInfo);

    /**
     * Remove a slave device from the topology
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    public void removeSlaveDevice(DeviceIdentifier slaveIdentifier);

    /**
     * @return a list containing additional {@link CollectedDeviceInfo} collected data,
     * this could be an ip address / protocol property / ... for a device
     */
    public List<CollectedDeviceInfo> getAdditionalCollectedDeviceInfo();

    /**
     * Add a new {@link CollectedDeviceInfo} collected data to the collected topology
     */
    public void addAdditionalCollectedDeviceInfo(CollectedDeviceInfo additionalDeviceInfo);

    /**
     * Getter for the {@link TopologyAction}
     */
    public TopologyAction getTopologyAction();

    /**
     * Setter for the {@link TopologyAction}
     *
     * @param topologyAction
     */
    public void setTopologyAction(TopologyAction topologyAction);

    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration);

    public void addPathSegmentFor(DeviceIdentifier source, DeviceIdentifier target, DeviceIdentifier intermediateHop, Duration timeToLive, int cost);

    public void addTopologyNeighbour(DeviceIdentifier neighbour, int modulationSchema, long toneMap, int modulation, int txGain, int txRes, int txCoeff, int lqi, int phaseDifferential, int tmrValidTime, int neighbourValidTime);

    public void addG3IdentificationInformation(String formattedIPv6Address, int ipv6ShortAddress, int logicalDeviceId);

    public List<TopologyPathSegment> getTopologyPathSegments();

    public List<TopologyNeighbour> getTopologyNeighbours();

    public G3TopologyDeviceAddressInformation getG3TopologyDeviceAddressInformation();
}