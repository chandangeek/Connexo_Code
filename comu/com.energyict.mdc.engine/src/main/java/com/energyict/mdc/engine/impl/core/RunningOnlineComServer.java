/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.impl.web.queryapi.WebSocketQueryApiService;

/**
 * Models the aspects of a {@link com.energyict.mdc.engine.config.OnlineComServer} that is actually running.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-16 (10:07)
 */
public interface RunningOnlineComServer extends RunningComServer {

    @Override
    OnlineComServer getComServer();

    void queryApiClientRegistered();

    void queryApiClientUnregistered();

    void queryApiCallCompleted(long executionTimeInMillis);

    void queryApiCallFailed(long executionTimeInMillis);

    WebSocketQueryApiService newWebSocketQueryApiService();

}