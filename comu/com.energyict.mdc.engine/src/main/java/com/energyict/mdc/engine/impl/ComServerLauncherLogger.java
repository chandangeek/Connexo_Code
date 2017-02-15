/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.io.IOException;

/**
 * Defines log message that are used by the {@link ComServerLauncher} class.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (15:07)
 */
public interface ComServerLauncherLogger {

    @Configuration(format = "Properties file for remote ComServer detected!", logLevel = LogLevel.DEBUG)
    void remoteComServerPropertiesDetected();

    @Configuration(format = "Remote query API URL found in properties file: {0}", logLevel = LogLevel.DEBUG)
    void remoteQueryAPIURLPropertyFound(String remoteQueryApiUrl);

    @Configuration(format = "Failed to start remote query API on URL {1}", logLevel = LogLevel.ERROR)
    void failedToStartQueryApi(Throwable cause, String remoteQueryApiUrl);

    @Configuration(format = "ComServer for host {0} not found!", logLevel = LogLevel.INFO)
    void comServerNotFound(String hostName);

    @Configuration(format = "ComServer {0} is not active.", logLevel = LogLevel.INFO)
    void comServerNotActive(String hostName);

    @Configuration(format = "Starting ComServer {0}...", logLevel = LogLevel.INFO)
    void starting(String name);

    @Configuration(format = "Starting ComServer {0} is delayed because no inbound device protocol services are active yet", logLevel = LogLevel.INFO)
    void startingDelayedBecauseOfMisingInboundDeviceProtocolServices(String name);

    @Configuration(format = "Starting ComServer {0} is delayed because no device protocol services are active yet", logLevel = LogLevel.INFO)
    void startingDelayedBecauseOfMisingDeviceProtocolServices(String name);

    @Configuration(format = "Starting ComServer {0} is delayed because no connection type services are active yet", logLevel = LogLevel.INFO)
    void startingDelayedBecauseOfMisingConnectionTypeServices(String name);

    @Configuration(format = "The ComServerLauncher is configured to start an online ComServer. This is a {0}", logLevel = LogLevel.ERROR)
    void notAnOnlineComeServer(String comServerClassName);

    @Configuration(format = "The ComServerLauncher is configured to start an remote ComServer. This is a {0}", logLevel = LogLevel.ERROR)
    void notARemoteComeServer(String comServerClassName);

    @Configuration(format = "Failure to load properties for remote ComServer", logLevel = LogLevel.ERROR)
    void failedToLoadRemoteComServerProperties(IOException e);

}