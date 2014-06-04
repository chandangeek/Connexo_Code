package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.util.time.Interval;

import java.util.List;

/**
 * Models the communication topology for a {@link Device} as a tree.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (10:27)
 */
public interface CommunicationTopology {

    /**
     * Gets the root of this CommunicaitonTopology.
     *
     * @return The root of this CommunicationTopology
     */
    public Device getRoot();

    /**
     * Gets the {@link Interval} during which this CommunicationTopology was effective.
     *
     * @return The Interval
     */
    public Interval getInterval();

    /**
     * Gets the first level {@link Device}s that are part of this CommunicationTopology,
     * i.e. the Devices whose direct communication gateway is the root of this CommunicationTopology.
     *
     * @return All the devices
     */
    public List<Device> getDevices();

    /**
     * Tests if this CommunicationTopology is a leaf in a bigger
     * CommunicationTopology, i.e. if it has children.
     *
     * @return A flag that indicates if this CommunicationTopology is a leaf and therefore does not have any children
     */
    public boolean isLeaf ();

    /**
     * Gets the child tree nodes of this CommunicationTopology.
     *
     * @return The child tree nodes
     */
    public List<CommunicationTopology> getChildren ();

    /**
     * Adds the CommunicationTopology as a child to this CommunicationTopology.
     * if there was already another CommunicationTopology child with the same
     * Interval then the new child is merged into the existing child.
     *
     * @param child The CommunicationTopology
     * @return <code>true</code> iff the Interval of the new child did not exist yet
     */
    public boolean addChild (CommunicationTopology child);

}