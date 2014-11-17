package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.TopologyTimeslice;

/**
 * Add behavior to {@link TopologyTimeslice} that is specifid
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-07 (15:54)
 */
public interface ServerTopologyTimeslice extends TopologyTimeslice {

    public CompleteTopologyTimesliceImpl asCompleteTimeslice();

}