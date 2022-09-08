/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.coap;

import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;

/**
 * Provides factory services for {@link EmbeddedCoapServer}s.
 */
public interface EmbeddedCoapServerFactory {

    /**
     * Finds or creates the {@link EmbeddedCoapServer} that hosts
     * the resource that supports inbound communication
     * on the specified ServerCoapBasedInboundComPort ComPort.
     *
     * @param comPort               The ServerCoapBasedInboundComPort
     * @param comServerDAO          The ComServerDAO
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param serviceProvider       The IssueService
     * @return The EmbeddedCoapServer
     */
    public EmbeddedCoapServer findOrCreateFor(CoapBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider);
}