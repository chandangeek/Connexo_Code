/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.events.EventRegistrationRequestInitiator;
import com.energyict.mdc.engine.impl.events.EventPublisher;


/**
 * Represents a request from an interested party to
 * register an interest in events that occur
 * in the {@link com.energyict.mdc.engine.config.ComServer}.
 * Note that it is allowed to register multiple times and narrow down
 * the interests to limit the amount of events received.
 * Note also that the interest can be registered multiple
 * time so e.g. change the interest in events that
 * relate to one {@link com.energyict.mdc.protocol.api.device.BaseDevice device} to another device.
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

}