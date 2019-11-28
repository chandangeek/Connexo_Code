/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.engine.events.EventRegistrationRequestInitiator;
import com.energyict.mdc.engine.impl.events.EventPublisher;


/**
 * Represents a request from an interested party to
 * register an interest in events that occur
 * in the {@link ComServer}.
 * Note that it is allowed to register multiple times and narrow down
 * the interests to limit the amount of events received.
 * Note also that the interest can be registered multiple
 * time so e.g. change the interest in events that
 * relate to one {@link com.energyict.mdc.upl.meterdata.Device device} to another device.
 * <p>
 * Making the initial request is done through the
 * {@link EventRegistrationRequestInitiator}
 * which will give you the URL on which you can post Requests
 * via WebSocket technology.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (17:34)
 */
public interface Request {

    void applyTo(EventPublisher eventPublisher);

    /**
     * Marks the request to produce binary events
     * when publishing to the interested party
     * that posted this Request.
     *
     * @param flag The flag value
     */
    public void setBinaryEvents (boolean flag);

    /**
     * Tests if this Request was marked to produce
     * binary events when publishing to the interested
     * party that posted this Request.
     *
     * @return <code>true</code> iff this Request was marked to produce binary events
     */
    public boolean useBinaryEvents ();

}