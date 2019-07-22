package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import javax.servlet.http.HttpSession;
import java.net.HttpCookie;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WebSocketQueryApiCreator implements WebSocketCreator {

    private RunningOnlineComServer comServer;
    private QueryAPIStatistics queryAPIStatistics;
    private Map<String, WebSocketQueryApiService> queryApiServices = new HashMap<>();

    public WebSocketQueryApiCreator(RunningOnlineComServer comServer, QueryAPIStatistics queryAPIStatistics) {
        super();
        this.comServer = comServer;
        this.queryAPIStatistics = queryAPIStatistics;
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
            if (request.getCookies() != null) {
                for (HttpCookie each : request.getCookies()) {
                   /* if (RemoteComServerDAOImpl.CLIENT_PROPERTY.equals(each.getName())) {
                        if (queryAPIStatistics != null) {
                            queryAPIStatistics.clientRegistered(each.getValue(), new Date(request.getSession().getLastAccessedTime()));
                        }
                    }*/
                }
            }
        }
        return queryApiService;
    }

    private WebSocketQueryApiService createQueryApiService() {
       return null;
       // return WebSocketQueryApiServiceFactory.getInstance().newWebSocketQueryApiService(this.comServer, queryAPIStatistics);
    }
}