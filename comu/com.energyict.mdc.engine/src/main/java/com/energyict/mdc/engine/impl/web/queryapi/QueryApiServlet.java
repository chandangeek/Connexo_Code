/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a servlet implementation for the ComServer remote query api.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-09 (16:30)
 */
public class QueryApiServlet extends WebSocketServlet {

    private QueryAPIStatistics queryAPIStatistics;
    private RunningOnlineComServer runningOnlineComServer;
    private Map<String, WebSocketQueryApiService> queryApiServices = new HashMap<>();

    public QueryApiServlet (RunningOnlineComServer comServer, QueryAPIStatistics queryAPIStatistics) {
        super();
        this.runningOnlineComServer = comServer;
        this.queryAPIStatistics = queryAPIStatistics;
    }

    public OnlineComServer getOnlineComServer() {
        return runningOnlineComServer.getComServer();
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setCreator(new WebSocketQueryApiCreator(runningOnlineComServer, queryAPIStatistics));
    }

    public WebSocketQueryApiService findOrCreateQueryApiService (HttpServletRequest request) {
        HttpSession httpSession = request.getSession(true);
        String httpSessionId = httpSession.getId();
        WebSocketQueryApiService queryApiService = this.queryApiServices.get(httpSessionId);
        if (queryApiService == null) {
            queryApiService = this.createQueryApiService();
            this.queryApiServices.put(httpSessionId, queryApiService);
        }
        return queryApiService;
    }

    private WebSocketQueryApiService createQueryApiService () {
        WebSocketQueryApiService queryApiService = this.runningOnlineComServer.newWebSocketQueryApiService();
        this.runningOnlineComServer.queryApiClientRegistered();
        return queryApiService;
    }

}