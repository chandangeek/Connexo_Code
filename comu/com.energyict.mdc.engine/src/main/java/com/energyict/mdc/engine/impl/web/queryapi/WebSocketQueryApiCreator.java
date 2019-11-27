package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import javax.servlet.http.HttpSession;
import java.net.HttpCookie;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a servlet implementation for the ComServer remote query api.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-09 (16:30)
 */
public class WebSocketQueryApiCreator implements WebSocketCreator {

    public final static String CLIENT_PROPERTY = "client-name";

    private QueryAPIStatistics queryAPIStatistics;
    private RunningOnlineComServer runningOnlineComServer;
    private Map<String, WebSocketQueryApiService> queryApiServices = new HashMap<>();

    public WebSocketQueryApiCreator(RunningOnlineComServer comServer, QueryAPIStatistics queryAPIStatistics) {
        super();
        this.runningOnlineComServer = comServer;
        this.queryAPIStatistics = queryAPIStatistics;
    }

    public OnlineComServer getOnlineComServer() {
        return runningOnlineComServer.getComServer();
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
                    if (CLIENT_PROPERTY.equals(each.getName())) {
                        if (queryAPIStatistics != null) {
                            queryAPIStatistics.clientRegistered(each.getValue(), new Date(request.getSession().getLastAccessedTime()));
                        }
                    }
                }
            }
        }
        return queryApiService;
    }

    private WebSocketQueryApiService createQueryApiService() {
        return WebSocketQueryApiServiceFactory.getInstance().newWebSocketQueryApiService(this.runningOnlineComServer, queryAPIStatistics);
    }
}