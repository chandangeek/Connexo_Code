/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.energyict.mdc.protocol.api.tasks.TopologyAction;

/**
 * Models the {@link com.energyict.mdc.tasks.ProtocolTask} which can check/update the Topology of a Device.
 *
 * @author gna
 * @since 19/04/12 - 13:56
 */
public interface TopologyTask extends ProtocolTask {

    /**
     * Return the TopologyAction for this task
     *
     * @return the TopologyAction
     */
    public TopologyAction getTopologyAction();
    public void setTopologyAction(TopologyAction topologyAction);
}