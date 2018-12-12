package com.energyict.mdc.upl.tasks.support;

import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.issue.Issue;

/**
 * Defines proper functionality to handle the Topology of a Device.
 */
public interface DeviceTopologySupport {

    /**
     * Collect the actual Topology from a Device.
     * If for some reason the Topology could not be fetched,
     * a proper {@link ResultType} <b>and</b> {@link Issue}
     * should be set so proper logging of this action can be performed.
     *
     * @return the current Topology
     */
    CollectedTopology getDeviceTopology();

}