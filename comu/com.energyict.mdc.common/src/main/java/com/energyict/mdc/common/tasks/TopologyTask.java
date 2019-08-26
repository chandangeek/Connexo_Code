/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.energyict.mdc.upl.tasks.TopologyAction;

/**
 * Models the {@link ProtocolTask} which can check/update the Topology of a Device.
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
    TopologyAction getTopologyAction();
    void setTopologyAction(TopologyAction topologyAction);
}