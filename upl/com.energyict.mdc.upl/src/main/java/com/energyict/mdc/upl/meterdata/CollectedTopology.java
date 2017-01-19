package com.energyict.mdc.upl.meterdata;

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
     * <p>
     * The ComServer framework will take the provided LastSeenDate info into account when updating the GW link of the slaves.
     * <p>
     * If this device has no attached slaves, the return list is empty.
     */
    Map<DeviceIdentifier, ObservationTimestampProperty> getSlaveDeviceIdentifiers();

    /**
     * Add a slave device to the topology, without any information on its "last seen date".
     *
     * @param slaveIdentifier the device identifier of the slave device
     */
    void addSlaveDevice(DeviceIdentifier slaveIdentifier);

    /**
     * Add a slave device to the topology
     *
     * @param slaveIdentifier              the device identifier of the slave device
     * @param observationTimestampProperty information on when this slave device was last seen by the gateway/DC.
     */
    void addSlaveDevice(DeviceIdentifier slaveIdentifier, ObservationTimestampProperty observationTimestampProperty);

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

    void addTopologyNeighbour(DeviceIdentifier neighbour, int modulationSchema, long toneMap, int modulation, int txGain, int txRes, int txCoeff, int lqi, int phaseDifferential, int tmrValidTime, int neighbourValidTime);

    void addG3IdentificationInformation(String formattedIPv6Address, int ipv6ShortAddress, int logicalDeviceId);

    List<TopologyPathSegment> getTopologyPathSegments();

    List<TopologyNeighbour> getTopologyNeighbours();

    G3TopologyDeviceAddressInformation getG3TopologyDeviceAddressInformation();

    /**
     * Models the timestamp on which a slave device was last observed.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2016-11-25 (13:51)
     */
    interface ObservationTimestampProperty {
        String getName();

        Date getValue();
    }
}