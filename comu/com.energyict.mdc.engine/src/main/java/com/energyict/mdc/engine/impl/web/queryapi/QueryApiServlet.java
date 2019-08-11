/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a servlet implementation for the ComServer remote query api.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-09 (16:30)
 */
public class QueryApiServlet extends WebSocketServlet {

    private RunningOnlineComServer comServer;
    private QueryAPIStatistics queryAPIStatistics;

    private final ComServerDAO comServerDAO;
    private final EngineConfigurationService engineConfigurationService;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;
    private final TransactionService transactionService;
    private Map<String, WebSocketQueryApiService> queryApiServices = new HashMap<>();

    public QueryApiServlet (RunningOnlineComServer comServer, ComServerDAO comServerDAO, EngineConfigurationService engineConfigurationService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, TransactionService transactionService) {
        super();
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
        this.engineConfigurationService = engineConfigurationService;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.transactionService = transactionService;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setCreator(new WebSocketQueryApiCreator(comServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService));
    }


}