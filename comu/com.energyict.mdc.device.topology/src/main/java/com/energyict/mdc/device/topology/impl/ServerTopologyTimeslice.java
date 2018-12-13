/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.topology.TopologyTimeslice;

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