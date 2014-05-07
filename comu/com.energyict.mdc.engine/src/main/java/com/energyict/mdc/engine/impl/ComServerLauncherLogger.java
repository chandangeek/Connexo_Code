package com.energyict.mdc.engine.impl;

import com.energyict.mdc.common.DatabaseException;
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
    public void remoteComServerPropertiesDetected ();

    @Configuration(format = "Remote query API URL found in properties file: {0}", logLevel = LogLevel.DEBUG)
    public void remoteQueryAPIURLPropertyFound (String remoteQueryApiUrl);

    @Configuration(format = "Failed to start remote query API on URL {1}", logLevel = LogLevel.ERROR)
    public void failedToStartQueryApi (Throwable cause, String remoteQueryApiUrl);

    @Configuration(format = "ComServer for host {0} not found!", logLevel = LogLevel.INFO)
    public void comServerNotFound (String hostName);

    @Configuration(format = "ComServer {0} is not active.", logLevel = LogLevel.INFO)
    public void comServerNotActive (String hostName);

    @Configuration(format = "Starting ComServer {0}...", logLevel = LogLevel.INFO)
    public void starting (String name);

    @Configuration(format = "The ComServerLauncher is configured to start an online ComServer. This is a {0}", logLevel = LogLevel.ERROR)
    public void notAnOnlineComeServer (String comServerClassName);

    @Configuration(format = "The ComServerLauncher is configured to start an remote ComServer. This is a {0}", logLevel = LogLevel.ERROR)
    public void notARemoteComeServer (String comServerClassName);

    @Configuration(format = "Failure to load properties for remote ComServer", logLevel = LogLevel.ERROR)
    public void failedToLoadRemoteComServerProperties (IOException e);

    @Configuration(format = "Connect database:Attempt {0} of {1}", logLevel = LogLevel.DEBUG)
    public void databaseConnectionAttempt (int attempt, int maxAttempts);

    @Configuration(format = "Connection to database failed, next attempt in {0} seconds!", logLevel = LogLevel.DEBUG)
    public void databaseConnectionFailed (int seconds, DatabaseException e);

}