package com.energyict.mdc.protocol.tasks.support;


import com.energyict.mdc.protocol.device.data.CollectedTopology;

/**
 * Defines proper functionality to handle the Topology of a Device
 */
public interface DeviceTopologySupport {

    /**
     * Collect the actual Topology from a Device. If for some reason the Topology could not be fetched,
     * a proper {@link com.energyict.mdc.protocol.device.data.ResultType resultType} <b>and</b> {@link com.energyict.mdc.issues.Issue issue}
     * should be set so proper logging of this action can be performed.
     *
     * @return the current Topology
     */
    public CollectedTopology getDeviceTopology();

}
