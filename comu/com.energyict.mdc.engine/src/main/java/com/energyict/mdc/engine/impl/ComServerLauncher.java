package com.energyict.mdc.engine.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningRemoteComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.meterdata.DefaultCollectedDataFactoryProvider;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.HostName;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Clock;
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
    private final RunningComServerImpl.ServiceProvider serviceProvider;

    private ComServerLauncherLogger logger;
    private String remoteQueryApiUrl;
    private RunningComServer runningComServer;

    public ComServerLauncher(RunningComServerImpl.ServiceProvider serviceProvider) {
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
        this.initializeProviders();
        if (this.shouldStartRemote()) {
            this.startRemoteComServer();
        } else {
            this.startOnlineComServer();
        }
    }

    private void initializeProviders() {
        CollectedDataFactoryProvider.instance.set(new DefaultCollectedDataFactoryProvider());
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
        ComServerDAO comServerDAO = new ComServerDAOImpl(new ComServerDaoServiceProvider());
        ComServer thisComServer = comServerDAO.getThisComServer();
        if (thisComServer == null) {
            this.logger.comServerNotFound(HostName.getCurrent());
        } else if (!thisComServer.isActive()) {
            this.logger.comServerNotActive(HostName.getCurrent());
        } else {
            if (thisComServer.isOnline()) {
                this.logger.starting(thisComServer.getName());
                this.runningComServer = new RunningOnlineComServerImpl((OnlineComServer) thisComServer, serviceProvider);
                this.runningComServer.start();
            } else {
                this.logger.notAnOnlineComeServer(thisComServer.getClass().getSimpleName());
            }
        }
    }

    private void startRemoteComServer () {
        RemoteComServerDAOImpl comServerDAO = new RemoteComServerDAOImpl(this.remoteQueryApiUrl, new RemoteComServerDaoServiceProvider());
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
                    this.runningComServer = new RunningRemoteComServerImpl((RemoteComServer) thisComServer, comServerDAO, serviceProvider);
                    this.runningComServer.start();
                } else {
                    this.logger.notARemoteComeServer(thisComServer.getClass().getSimpleName());
                }
            }
        } catch (ApplicationException e) {
            this.logger.failedToStartQueryApi(e.getCause(), this.remoteQueryApiUrl);
        }
    }

    private class ComServerDaoServiceProvider implements ComServerDAOImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public EngineModelService engineModelService() {
            return serviceProvider.engineModelService();
        }

        @Override
        public DeviceDataService deviceDataService() {
            return serviceProvider.deviceDataService();
        }

        @Override
        public TransactionService transactionService() {
            return serviceProvider.transactionService();
        }

        @Override
        public EngineService engineService() {
            return serviceProvider.engineService();
        }

        @Override
        public EventService eventService() {
            return serviceProvider.eventService();
        }

    }

    private class RemoteComServerDaoServiceProvider implements RemoteComServerDAOImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public EngineModelService engineModelService() {
            return serviceProvider.engineModelService();
        }

    }

}