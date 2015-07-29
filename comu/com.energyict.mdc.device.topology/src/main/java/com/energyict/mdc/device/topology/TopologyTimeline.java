package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Models timeline of the topology of a {@link Device} up to one level deep.
 * A TopologyTimeslice is a collection of Devices that are directly
 * referencing a Device as a gateway. A device is directly referencing
 * another device when {@link TopologyService#getPhysicalGateway(Device)}
 * returns that Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-02 (16:04)
 */
@ProviderType
public interface TopologyTimeline {

    /**
     * Gets the different {@link TopologyTimeslice}s.
     * The periods of each of the slices are not overlapping.
     * The periods may or may not abut however.
     * If during a certain period in time, the topology
     * was empty, i.e. no {@link Device}s were directly
     * referencing the gateway, there will be no slice for that.
     *
     * @return The slices
     */
    public List<TopologyTimeslice> getSlices();

    /**
     * Gets the set of all {@link Device}s that are part of the timeline.
     * You can visit each Device and call {@link #addedFirstOn(Device)}
     * or {@link #mostRecentlyAddedOn(Device)} to find out when it was added
     * first or most recently to the topology.
     *
     * @return The Set of Devices
     */
    public Set<Device> getAllDevices();

    /**
     * Gets the instant in time on which the specified {@link Device}
     * was most recently added to the timeline.
     *
     * @param device The Device
     * @return The instant in time
     */
    public Optional<Instant> mostRecentlyAddedOn(Device device);

    /**
     * Gets the instant in time on which the specified {@link Device}
     * was added to the timeline for the first time.
     *
     * @param device The Device
     * @return The instant in time
     */
    public Optional<Instant> addedFirstOn(Device device);

}