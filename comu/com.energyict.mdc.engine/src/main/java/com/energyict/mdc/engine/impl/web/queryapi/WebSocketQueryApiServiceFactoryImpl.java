package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;

import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link WebSocketQueryApiServiceFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-09 (11:20)
 */
@Component(name = "com.energyict.mdc.engine.queryapi.websocket.service.factory", service = WebSocketQueryApiServiceFactory.class, immediate = true)
public class WebSocketQueryApiServiceFactoryImpl implements WebSocketQueryApiServiceFactory {

    @Override
    public WebSocketQueryApiService newWebSocketQueryApiService(RunningOnlineComServer comServer) {
        return new WebSocketQueryApiService(comServer);
    }

}