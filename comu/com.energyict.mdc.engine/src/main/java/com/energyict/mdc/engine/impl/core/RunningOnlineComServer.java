package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.web.queryapi.WebSocketQueryApiService;
import com.energyict.mdc.engine.model.OnlineComServer;

/**
 * Models the aspects of a {@link com.energyict.mdc.engine.model.OnlineComServer} that is actually running.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-16 (10:07)
 */
public interface RunningOnlineComServer extends RunningComServer {

    @Override
    public OnlineComServer getComServer();

    public void queryApiClientRegistered();

    public void queryApiClientUnregistered();

    public void queryApiCallCompleted(long executionTimeInMillis);

    public void queryApiCallFailed(long executionTimeInMillis);

    public WebSocketQueryApiService newWebSocketQueryApiService();

}