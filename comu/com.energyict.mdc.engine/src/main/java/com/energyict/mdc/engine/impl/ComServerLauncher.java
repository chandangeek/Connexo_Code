package com.energyict.mdc.engine.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.OnlineRunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RemoteRunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.tools.Strings;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.HostName;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;

import com.elster.jupiter.util.Checks;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launches the {@link com.energyict.mdc.engine.model.OnlineComServer}
 * or {@link com.energyict.mdc.engine.model.RemoteComServer}
 * that is configured to run on the machine where this class is executing.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:43)
 */
public final class ComServerLauncher {

    private static final String LOG4J_PROPERTIES_FILE_NAME = "./comserver-log4j.properties";
    private static final int MAXIMUM_CONNECT_ATTEMPTS = 6;
    private final ServiceProvider serviceProvider;

    private ComServerLauncherLogger logger;
    private String remoteQueryApiUrl;
    private RunningComServer runningComServer;

    public ComServerLauncher(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        initializeLogging();
    }

    private void initializeLogging () {
        this.initializeLog4J();
        Logger anonymousLogger = Logger.getAnonymousLogger();
        anonymousLogger.setLevel(Level.FINEST);
        this.logger = LoggerFactory.getLoggerFor(ComServerLauncherLogger.class, anonymousLogger);
    }

    private void initializeLog4J () {
        try {
            URL configURL = this.getClass().getClassLoader().getResource(LOG4J_PROPERTIES_FILE_NAME);
            if (configURL == null) {
                this.initializeLog4JWithFingersCrossed();
            } else {
                PropertyConfigurator.configure(configURL);
                File configFile = new File(configURL.toURI());
                PropertyConfigurator.configureAndWatch(configFile.getAbsolutePath());
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            this.initializeLog4JWithFingersCrossed();
        }
    }

    private void initializeLog4JWithFingersCrossed () {
        PropertyConfigurator.configureAndWatch(LOG4J_PROPERTIES_FILE_NAME);
    }

    public void startComServer () {
        this.attemptLoadRemoteComServerProperties();
        if (this.shouldStartRemote()) {
            this.startRemoteComServer();
        } else {
            this.startOnlineComServer();
        }
    }

    public void stopComServer(){
        if(this.runningComServer != null){
            this.runningComServer.shutdownImmediate();
        }
    }

    private boolean shouldStartRemote () {
        return !Checks.is(this.remoteQueryApiUrl).empty();
    }

    private InputStream getRemoteComServerPropertiesStream() throws FileNotFoundException {
        return new FileInputStream("comserver.properties");
    }

    private void attemptLoadRemoteComServerProperties() {
        try (InputStream comserverPropertiesStream = this.getRemoteComServerPropertiesStream()) {
            if (comserverPropertiesStream != null) {
                try {
                    Properties remoteComServerProperties = new Properties();
                    remoteComServerProperties.load(comserverPropertiesStream);
                    this.remoteQueryApiUrl = remoteComServerProperties.getProperty("remoteQueryApiUrl");
                    this.logger.remoteComServerPropertiesDetected();
                    this.logger.remoteQueryAPIURLPropertyFound(this.remoteQueryApiUrl);
                } catch (IOException e) {
                    this.logger.failedToLoadRemoteComServerProperties(e);
                    this.remoteQueryApiUrl = null;
                }
            }
        } catch (IOException e) {
            // could not load file, not remote ComServer
        }
    }

    private void startOnlineComServer() {
        ComServerDAO comServerDAO = new ComServerDAOImpl(serviceProvider);
        ComServer thisComServer = comServerDAO.getThisComServer();
        if (thisComServer == null) {
            this.logger.comServerNotFound(HostName.getCurrent());
        } else if (!thisComServer.isActive()) {
            this.logger.comServerNotActive(HostName.getCurrent());
        } else {
            if (thisComServer.isOnline()) {
                this.logger.starting(thisComServer.getName());
                this.runningComServer = new OnlineRunningComServerImpl((OnlineComServer) thisComServer, serviceProvider);
                this.runningComServer.start();
            } else {
                this.logger.notAnOnlineComeServer(thisComServer.getClass().getSimpleName());
            }
        }
    }

    private void startRemoteComServer () {
        RemoteComServerDAOImpl comServerDAO = new RemoteComServerDAOImpl(this.remoteQueryApiUrl, serviceProvider);
        try {
            comServerDAO.start();
            String hostNameOfThisMachine = HostName.getCurrent();
            ComServer thisComServer = comServerDAO.getComServer(hostNameOfThisMachine);
            if (thisComServer == null) {
                this.logger.comServerNotFound(hostNameOfThisMachine);
            } else if (!thisComServer.isActive()) {
                this.logger.comServerNotActive(hostNameOfThisMachine);
            } else {
                if (thisComServer.isRemote()) {
                    this.logger.starting(thisComServer.getName());
                    this.runningComServer = new RemoteRunningComServerImpl((RemoteComServer) thisComServer, comServerDAO, serviceProvider);
                    this.runningComServer.start();
                } else {
                    this.logger.notARemoteComeServer(thisComServer.getClass().getSimpleName());
                }
            }
        } catch (ApplicationException e) {
            this.logger.failedToStartQueryApi(e.getCause(), this.remoteQueryApiUrl);
        }
    }

}