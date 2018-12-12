/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.registration;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.events.EventRegistrationRequestInitiator;

import java.util.Optional;

/**
 * Provides the default implementation for the {@link EventRegistrationRequestInitiator} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (13:44)
 */
public class EventRegistrationRequestInitiatorImpl implements EventRegistrationRequestInitiator {

    private final EngineConfigurationService engineConfigurationService;

    public EventRegistrationRequestInitiatorImpl(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Override
    public String getRegistrationURL (String comServerName) {
        Optional<ComServer> comServer = engineConfigurationService.findComServer(comServerName);
        if (!comServer.isPresent()) {
            throw new IllegalArgumentException("The Comserver by the name of {0} does not exist");
        }
        else {
            return this.getRegistrationURL(comServer.get());
        }
    }

    @Override
    public String getRegistrationURL (ComServer comServer) {
        return comServer.getEventRegistrationUriIfSupported();
    }

}