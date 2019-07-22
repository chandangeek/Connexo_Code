/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;

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
    private Map<String, WebSocketQueryApiService> queryApiServices = new HashMap<>();

    public QueryApiServlet (RunningOnlineComServer comServer, QueryAPIStatistics queryAPIStatistics) {
        super();
        this.comServer = comServer;
        this.queryAPIStatistics = queryAPIStatistics;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.setCreator(new WebSocketQueryApiCreator(comServer, queryAPIStatistics));
    }


}