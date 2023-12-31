/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.FoundUserIsNotActiveException;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundCapable;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundCapable;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningRemoteComServerImpl;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.offline.OfflineComServerProperties;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.impl.core.remote.RemoteProperties;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListener;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.Services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

/**
 * Launches the {@link OnlineComServer}
 * or {@link RemoteComServer}
 * that is configured to run on the machine where this class is executing.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:43)
 */
public final class ComServerLauncher implements ProtocolDeploymentListener {

    private final RunningComServerImpl.ServiceProvider serviceProvider;

    private ComServerLauncherLogger logger;
    private String remoteQueryApiUrl;
    private ComServer comServer;
    private RunningComServer runningComServer;
    private Counter deviceProtocolServices = Counters.newLenientNonNegativeCounter();
    private Counter inboundDeviceProtocolServices = Counters.newLenientNonNegativeCounter();
    private Counter connectionTypeServices = Counters.newLenientNonNegativeCounter();
    private User comserverUser;

    public ComServerLauncher(RunningComServerImpl.ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        initializeLogging();
        initComserverUser();
    }

    @Override
    public void deviceProtocolServiceDeployed(DeviceProtocolService service) {
        this.deviceProtocolServices.increment();
        this.doStartComServer();
    }

    @Override
    public void deviceProtocolServiceUndeployed(DeviceProtocolService service) {
        this.deviceProtocolServices.decrement();
    }

    @Override
    public void inboundDeviceProtocolServiceDeployed(InboundDeviceProtocolService service) {
        this.inboundDeviceProtocolServices.increment();
        this.doStartComServer();
    }

    @Override
    public void inboundDeviceProtocolServiceUndeployed(InboundDeviceProtocolService service) {
        this.inboundDeviceProtocolServices.decrement();
    }

    @Override
    public void connectionTypeServiceDeployed(ConnectionTypeService service) {
        this.connectionTypeServices.increment();
        this.doStartComServer();
    }

    @Override
    public void connectionTypeServiceUndeployed(ConnectionTypeService service) {
        this.connectionTypeServices.decrement();
    }

    private void initializeLogging() {
        this.logger = LoggerFactory.getLoggerFor(ComServerLauncherLogger.class);
    }

    private void initComserverUser() {
        try {
            comserverUser = serviceProvider.userService().findUser(EngineServiceImpl.COMSERVER_USER).get();
        } catch (FoundUserIsNotActiveException e) {
            logger.userComServerNotActive(HostName.getCurrent(), EngineServiceImpl.COMSERVER_USER);
            throw e;
        }
    }

    public void startComServer() {
        this.attemptLoadRemoteComServerProperties();
        this.doStartComServer();
    }

    private void doStartComServer() {
        if (!isStarted()) {
            if (this.shouldStartRemote()) {
                this.startRemoteComServer();
            } else {
                this.startOnlineComServer();
            }
        }
    }

    public boolean isStarted() {
        Set<ServerProcessStatus> startedStatusses = EnumSet.of(ServerProcessStatus.STARTING, ServerProcessStatus.STARTED);
        return this.runningComServer != null
                && startedStatusses.contains(runningComServer.getStatus());
    }

    public void stopComServer() {
        if (this.isStarted()) {
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
        } catch (IOException e) {
            // could not load file, not remote ComServer
        }
    }

    private void startOnlineComServer() {
        Services.objectMapperService(new ObjectMapperServiceImpl(new OnlineJSONTypeMapper()));
        ComServerDAO comServerDAO = new ComServerDAOImpl(new ComServerDaoServiceProvider(), comserverUser); // we should always have the comserver user
        this.comServer = comServerDAO.getThisComServer();
        this.doStartOnlineComServer(comServerDAO);
    }

    private void doStartOnlineComServer(ComServerDAO comServerDAO) {
        if (this.comServer == null) {
            this.logger.comServerNotFound(HostName.getCurrent());
        } else if (!this.comServer.isActive()) {
            this.logger.comServerNotActive(HostName.getCurrent());
        } else {
            if (this.comServer.isOnline()) {
                if (this.validateServices((OnlineComServer) this.comServer)) {
                    this.logger.starting(this.comServer.getName());
                    this.runningComServer = new RunningOnlineComServerImpl((OnlineComServer) this.comServer, comServerDAO, serviceProvider);
                    this.runningComServer.start();
                }
            } else {
                this.logger.notAnOnlineComeServer(this.comServer.getClass().getSimpleName());
            }
        }
    }

    private boolean validateServices(OnlineComServer comServer) {
        return this.validateInboundServices(comServer, comServer.getName())
                && this.validateOutboundServices(comServer, comServer.getName());
    }

    private boolean validateServices(RemoteComServer comServer) {
        return this.validateInboundServices(comServer, comServer.getName())
                && this.validateOutboundServices(comServer, comServer.getName());
    }

    private boolean validateInboundServices(InboundCapable comServer, String comServerName) {
        if (!comServer.getInboundComPorts().isEmpty() && this.inboundDeviceProtocolServices.getValue() <= 0) {
            this.logger.startingDelayedBecauseOfMisingInboundDeviceProtocolServices(comServerName);
            return false;
        } else {
            return true;
        }
    }

    private boolean validateOutboundServices(OutboundCapable comServer, String comServerName) {
        if (!comServer.getOutboundComPorts().isEmpty()) {
            if (this.connectionTypeServices.getValue() <= 0) {
                this.logger.startingDelayedBecauseOfMisingConnectionTypeServices(comServerName);
                return false;
            }
            if (this.deviceProtocolServices.getValue() <= 0) {
                this.logger.startingDelayedBecauseOfMisingDeviceProtocolServices(comServerName);
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    private void startRemoteComServer() {
        Properties properties = OfflineComServerProperties.getInstance().getProperties();
        RemoteProperties remoteProperties = new RemoteProperties(properties);
        RemoteComServerDAOImpl comServerDAO = new RemoteComServerDAOImpl(remoteProperties, new RemoteComServerDaoServiceProvider());
        comServerDAO.start();
        this.comServer = comServerDAO.getComServer(HostName.getCurrent());
        this.doStartRemoteComServer(comServerDAO);
    }

    private void doStartRemoteComServer(RemoteComServerDAOImpl comServerDAO) {
        try {
            if (this.comServer == null) {
                this.logger.comServerNotFound(HostName.getCurrent());
            } else if (!this.comServer.isActive()) {
                this.logger.comServerNotActive(HostName.getCurrent());
            } else {
                if (this.comServer.isRemote()) {
                    if (this.validateServices((RemoteComServer) this.comServer)) {
                        this.logger.starting(this.comServer.getName());
                        this.runningComServer = new RunningRemoteComServerImpl((RemoteComServer) this.comServer, comServerDAO, serviceProvider);
                        this.runningComServer.start();
                    }
                } else {
                    this.logger.notARemoteComeServer(this.comServer.getClass().getSimpleName());
                }
            }
        } catch (ApplicationException e) {
            this.logger.failedToStartQueryApi(e.getCause(), this.remoteQueryApiUrl);
        }
    }

    private class ComServerDaoServiceProvider implements ComServerDAOImpl.ServiceProvider {

        @Override
        public Thesaurus thesaurus() {
            return serviceProvider.thesaurus();
        }

        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return serviceProvider.protocolPluggableService();
        }

        public DeviceMessageSpecificationService deviceMessageSpecificationService() {
            return serviceProvider.deviceMessageSpecificationService();
        }

        @Override
        public TopologyService topologyService() {
            return serviceProvider.topologyService();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return serviceProvider.mdcReadingTypeUtilService();
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
        public OrmService ormService() {
            return serviceProvider.ormService();
        }

        @Override
        public PriorityComTaskService priorityComTaskService() {
            return serviceProvider.priorityComTaskService();
        }

        @Override
        public DeviceService deviceService() {
            return serviceProvider.deviceService();
        }

        @Override
        public RegisterService registerService() {
            return serviceProvider.registerService();
        }

        @Override
        public LoadProfileService loadProfileService() {
            return serviceProvider.loadProfileService();
        }

        @Override
        public LogBookService logBookService() {
            return serviceProvider.logBookService();
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return serviceProvider.deviceMessageService();
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
        public UserService userService() {
            return serviceProvider.userService();
        }

        @Override
        public EventService eventService() {
            return serviceProvider.eventService();
        }

        @Override
        public IdentificationService identificationService() {
            return serviceProvider.identificationService();
        }

        @Override
        public FirmwareService firmwareService() {
            return serviceProvider.firmwareService();
        }

        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return serviceProvider.deviceConfigurationService();
        }

        @Override
        public SecurityManagementService securityManagementService() {
            return serviceProvider.securityManagementService();
        }

        @Override
        public CustomPropertySetService customPropertySetService() {
            return serviceProvider.customPropertySetService();
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

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return serviceProvider.threadPrincipalService();
        }

    }

}