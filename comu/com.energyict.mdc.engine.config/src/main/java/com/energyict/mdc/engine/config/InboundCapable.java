/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

import java.util.List;

/**
 * Models the behavior of a component that is capable of accepting inbound connections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (08:58)
 */
public interface InboundCapable {

    /**
     * Gets the {@link InboundComPort}s that are
     * will support the inbound connections.
     *
     * @return The list of InboundComPorts
     */
    public List<InboundComPort> getInboundComPorts ();

}