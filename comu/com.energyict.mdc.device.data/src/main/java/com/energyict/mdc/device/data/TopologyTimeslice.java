package com.energyict.mdc.device.data;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Models a slice of the {@link TopologyTimeline}.
 * A TopologyTimeslice is a collection of Devices that are directly
 * referencing a Device as a gateway. A device is directly referencing
 * another device when {@link Device#getCommunicationGateway()} or {@link Device#getPhysicalGateway()} returns that Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-02 (16:04)
 */
public interface TopologyTimeslice {

    /**
     * Gets the {@link Range} during which this slice was effective.
     *
     * @return The Range
     */
    public Range<Instant> getPeriod();

    public List<Device> getDevices ();

}