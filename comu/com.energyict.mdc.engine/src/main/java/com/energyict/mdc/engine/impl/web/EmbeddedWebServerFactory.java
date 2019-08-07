/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;

/**
 * Provides factory services for {@link EmbeddedWebServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-04 (12:03)
 */
public interface EmbeddedWebServerFactory {
    /**
     * Finds or creates the {@link EmbeddedWebServer} that hosts
     * the servlet that supports inbound communication
     * on the specified ServerServletBasedInboundComPort ComPort.
     *
     * @param comPort The ServerServletBasedInboundComPort
     * @param comServerDAO The ComServerDAO
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param serviceProvider The IssueService
     * @return The EmbeddedWebServer
     */
    public EmbeddedWebServer findOrCreateFor(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider);

    /**
     * Finds or creates the {@link EmbeddedWebServer} that hosts
     * the servlet that supports remote clients to register for
     * and receive events about what is going on in the specified
     * {@link com.energyict.mdc.engine.config.ComServer}.
     *
     * @param comServer The ComServer
     * @return The EmbeddedWebServer
     */
    public EmbeddedWebServer findOrCreateEventWebServer (ComServer comServer, EventAPIStatistics eventAPIStatistics);

    /**
     * Finds or creates the {@link EmbeddedWebServer} that hosts
     * the servlet that supports {@link com.energyict.mdc.engine.config.RemoteComServer}s
     * to execute queries using the specified
     * {@link com.energyict.mdc.engine.config.OnlineComServer}.
     *
     * @param comServer The RunningOnlineComServer
     * @return The EmbeddedWebServer
     */
    public EmbeddedWebServer findOrCreateRemoteQueryWebServer (RunningOnlineComServer comServer, ComServerDAO comServerDAO, EngineConfigurationService engineConfigurationService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, TransactionService transactionService);

}