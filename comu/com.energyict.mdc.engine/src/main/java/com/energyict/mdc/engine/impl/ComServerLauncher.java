package com.energyict.mdc.engine.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningRemoteComServerImpl;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launches the {@link com.energyict.mdc.engine.config.OnlineComServer}
 * or {@link com.energyict.mdc.engine.config.RemoteComServer}
 * that is configured to run on the machine where this class is executing.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:43)
 */
public final class ComServerLauncher {

    private final RunningComServerImpl.ServiceProvider serviceProvider;

    private ComServerLauncherLogger logger;
    private String remoteQueryApiUrl;
    private RunningComServer runningComServer;

    public ComServerLauncher(RunningComServerImpl.ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        initializeLogging();
    }

    private void initializeLogging() {
        Logger anonymousLogger = Logger.getAnonymousLogger();
        anonymousLogger.setLevel(Level.FINEST);
        this.logger = LoggerFactory.getLoggerFor(ComServerLauncherLogger.class, anonymousLogger);
    }

    public void startComServer() {
        this.attemptLoadRemoteComServerProperties();
        if (this.shouldStartRemote()) {
            this.startRemoteComServer();
        } else {
            this.startOnlineComServer();
        }
    }

    public boolean isStarted() {
        return this.runningComServer != null;
    }

    public void stopComServer() {
        if (this.runningComServer != null) {
            this.runningComServer.shutdownImmediate();
        }
    }

    private boolean shouldStartRemote() {
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

    private void startRemoteComServer() {
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
        public TopologyService topologyService() {
            return serviceProvider.topologyService();
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return serviceProvider.engineConfigurationService();
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return serviceProvider.connectionTaskService();
        }

        @Override
        public CommunicationTaskService communicationTaskService() {
            return serviceProvider.communicationTaskService();
        }

        @Override
        public DeviceService deviceService() {
            return serviceProvider.deviceService();
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

        @Override
        public IdentificationService identificationService() {
            return serviceProvider.identificationService();
        }

    }

    private class RemoteComServerDaoServiceProvider implements RemoteComServerDAOImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return serviceProvider.engineConfigurationService();
        }

    }

}