package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.model.OnlineComServer;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

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

    private OnlineComServer comServer;
    private Map<String, WebSocketQueryApiService> queryApiServices = new HashMap<>();

    public QueryApiServlet (OnlineComServer comServer) {
        super();
        this.comServer = comServer;
    }

    public OnlineComServer getComServer () {
        return comServer;
    }

    @Override
    public WebSocket doWebSocketConnect (HttpServletRequest request, String protocol) {
        return this.findOrCreateQueryApiService(request);
    }

    private WebSocketQueryApiService findOrCreateQueryApiService (HttpServletRequest request) {
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
        return WebSocketQueryApiServiceFactory.getInstance().newWebSocketQueryApiService(this.comServer);
    }

}