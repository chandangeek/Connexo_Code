package com.energyict.mdc.engine.impl.web.queryapi;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class WebSocketQueryApiCreator implements WebSocketCreator {

    private RunningOnlineComServer comServer;
    private QueryAPIStatistics queryAPIStatistics;
    private Map<String, WebSocketQueryApiService> queryApiServices = new HashMap<>();

    private final ComServerDAO comServerDAO;
    private final EngineConfigurationService engineConfigurationService;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;
    private final TransactionService transactionService;

    public WebSocketQueryApiCreator(RunningOnlineComServer comServer, ComServerDAO comServerDAO, EngineConfigurationService engineConfigurationService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, TransactionService transactionService) {
        super();
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
        this.engineConfigurationService = engineConfigurationService;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.transactionService = transactionService;
    }

    public RunningOnlineComServer getComServer() {
        return comServer;
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        return findOrCreateQueryApiService(servletUpgradeRequest);
    }

    private WebSocketQueryApiService findOrCreateQueryApiService(ServletUpgradeRequest request) {
        HttpSession httpSession = request.getHttpServletRequest().getSession(true);
        if (httpSession == null) {
            return null;
        }
        String httpSessionId = httpSession.getId();
        WebSocketQueryApiService queryApiService = this.queryApiServices.get(httpSessionId);
        if (queryApiService == null) {
            queryApiService = this.createQueryApiService();
            queryApiServices.put(httpSessionId, queryApiService);
        }
        return queryApiService;
    }

    private WebSocketQueryApiService createQueryApiService() {
       return WebSocketQueryApiServiceFactory.getInstance().newWebSocketQueryApiService(this.comServer, this.comServerDAO, this.engineConfigurationService, this.connectionTaskService, this.communicationTaskService, this.transactionService);
    }
}