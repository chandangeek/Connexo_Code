package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Models the topology for a {@link Device} as a tree.
 * A topology is defined as a collection of Devices that are directly
 * referencing a Device as a gateway.
 * A device is directly referencing another device when
 * {@link TopologyService#getCommunicationGateway()} or
 * {@link TopologyService#getPhysicalGateway(Device)} returns that Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (10:27)
 */
@ProviderType
public interface DeviceTopology {

    /**
     * Gets the root of this DeviceTopology.
     *
     * @return The root of this DeviceTopology
     */
    public Device getRoot();

    /**
     * Gets the {@link Interval} during which this DeviceTopology was effective.
     *
     * @return The Interval
     */
    public Range<Instant> getPeriod();

    /**
     * Gets the first level {@link Device}s that are part of this DeviceTopology,
     * i.e. the Devices whose direct communication gateway is the root of this DeviceTopology.
     *
     * @return All the devices
     */
    public List<Device> getDevices();

    /**
     * Gets the {@link Device}s on all levels of this DeviceTopology.
     *
     * @return All the devices
     */
    public Set<Device> getAllDevices();

    /**
     * Returns this DeviceTopology as a flat timeline.
     *
     * @return The TopologyTimeline
     */
    public TopologyTimeline timelined();

    /**
     * Tests if this DeviceTopology is a leaf in a bigger
     * DeviceTopology, i.e. if it does not have children.
     *
     * @return A flag that indicates if this DeviceTopology is a leaf and therefore does not have any children
     */
    public boolean isLeaf ();

    /**
     * Gets the child tree nodes of this DeviceTopology.
     *
     * @return The child tree nodes
     */
    public List<DeviceTopology> getChildren ();

}