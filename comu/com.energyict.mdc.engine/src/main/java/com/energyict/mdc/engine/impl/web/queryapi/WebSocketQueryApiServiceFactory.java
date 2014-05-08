package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.model.OnlineComServer;

/**
 * Provides factory services for {@link WebSocketQueryApiService}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-09 (11:20)
 */
public class WebSocketQueryApiServiceFactory {

    private static WebSocketQueryApiServiceFactory soleInstance;

    public static WebSocketQueryApiServiceFactory getInstance () {
        if (soleInstance == null) {
            soleInstance = new WebSocketQueryApiServiceFactory();
        }
        return soleInstance;
    }

    public static void setInstance (WebSocketQueryApiServiceFactory factory) {
        soleInstance = factory;
    }

    public WebSocketQueryApiService newWebSocketQueryApiService (OnlineComServer comServer) {
        return new WebSocketQueryApiService(comServer);
    }

    // Hide utility class constructor
    protected WebSocketQueryApiServiceFactory () {}

}