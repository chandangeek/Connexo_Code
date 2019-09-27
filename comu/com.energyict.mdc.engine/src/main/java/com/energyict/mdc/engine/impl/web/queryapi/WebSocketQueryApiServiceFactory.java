package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

/**
 * Provides factory services for {@link WebSocketQueryApiService}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-09 (11:20)
 */
public class WebSocketQueryApiServiceFactory {

    private static WebSocketQueryApiServiceFactory soleInstance;

    public static WebSocketQueryApiServiceFactory getInstance() {
        if (soleInstance == null) {
            soleInstance = new WebSocketQueryApiServiceFactory();
        }
        return soleInstance;
    }

    public static void setInstance(WebSocketQueryApiServiceFactory factory) {
        soleInstance = factory;
    }


    protected WebSocketQueryApiService newWebSocketQueryApiService(RunningOnlineComServer comServer) {
        return comServer.newWebSocketQueryApiService();
    }

    public WebSocketQueryApiService newWebSocketQueryApiService(RunningOnlineComServer comServer, QueryAPIStatistics queryAPIStatistics) {
        return comServer.newWebSocketQueryApiService();
        //        return new WebSocketQueryApiService(comServer, queryAPIStatistics);
    }

    // Hide utility class constructor
    protected WebSocketQueryApiServiceFactory() {
    }

}