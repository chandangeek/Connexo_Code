/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;


import java.util.List;

/**
 * Models the behavior of a component that is capable of setting up outbound connections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (08:58)
 */
public interface OutboundCapable {

    /**
     * Gets the {@link OutboundComPort}s that are
     * will support the outbound connections.
     *
     * @return The list of OutboundComPorts
     */
    public List<OutboundComPort> getOutboundComPorts ();

}