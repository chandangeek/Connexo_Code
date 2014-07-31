package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;

/**
 * Provides factory services for {@link WebSocketQueryApiService}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-16 (10:13)
 */
public interface WebSocketQueryApiServiceFactory {

    public WebSocketQueryApiService newWebSocketQueryApiService (RunningOnlineComServer comServer);

}