package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.energyict.mdc.engine.impl.LaunchComServer", service = LaunchComServer.class,
        property = {"osgi.command.scope=mdc",
                "osgi.command.function=launchComServer",
                "osgi.command.function=stopComServer",
                "osgi.command.function=lcs",
                "osgi.command.function=scs"},
        immediate = true)
@SuppressWarnings("unused")
public class LaunchComServer implements EngineService.DeactivationNotificationListener {

    private static final String PROPERTY_NAME_AUTO_START = "com.energyict.comserver.autostart";

    private volatile Clock clock;
    private volatile EventService eventService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile EngineService engineService;
    private volatile HexService hexService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile IssueService issueService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile LogBookService logBookService;
    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private volatile ManagementBeanFactory managementBeanFactory;
    private volatile UserService userService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile SocketService socketService;
    private volatile SerialComponentService serialATComponentService;
    private volatile MeteringService meteringService;

    private boolean autoStart;
    private ComServerLauncher launcher;

    @Activate
    public void activate(BundleContext context) {
        System.out.println("Activating ComServerLauncher test");
        autoStart = Boolean.parseBoolean(getOptionalProperty(context, PROPERTY_NAME_AUTO_START, "false"));
        if (autoStart) {
            launchComServer();
        }
    }

    @Deactivate
    public void deactivate() {
        System.out.println("Deactivating ComServerLauncher test");
        if (autoStart) {
            stopComServer();
        }
    }

    private String getOptionalProperty(BundleContext context, String property, String defaultValue) {
        String value = context.getProperty(property);
        return value == null ? defaultValue : value;
    }

    @SuppressWarnings("unused")
    public void stopComServer() {
        if (this.launcher != null) {
            this.engineService.unregister(this);
            System.out.println("Shutting down the ComServer, this may take a while ...");
            this.launcher.stopComServer();
            this.launcher = null;
            System.out.println("ComServer has been shut down...");
        } else {
            System.out.println("No ComServer available to shut down!");
            System.out.println("*** Use 'lsc' to start a ComServer on your local machine.");
        }
    }

    @SuppressWarnings("unused")
    public void scs() {
        stopComServer();
    }

    @SuppressWarnings("unused")
    public void lcs() {
        launchComServer();
    }

    @SuppressWarnings("unused")
    public void launchComServer() {
        if (this.launcher == null) {
            System.out.println("Starting ComServer");
            try {
                this.engineService.register(this);
                this.launcher = new ComServerLauncher(new RunningComServerServiceProvider());
                launcher.startComServer();
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } else {
            System.out.println("There is already a ComServer running on this machine.");
        }
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setHexService(HexService hexService) {
        this.hexService = hexService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    @Reference
    public void setManagementBeanFactory(ManagementBeanFactory managementBeanFactory) {
        this.managementBeanFactory = managementBeanFactory;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.AT + "))")
    public void setSerialATComponentService(SerialComponentService serialATComponentService) {
        this.serialATComponentService = serialATComponentService;
    }

    @Reference
    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void engineServiceDeactivationStarted() {
        this.stopComServer();
    }

    private class RunningComServerServiceProvider implements RunningComServerImpl.ServiceProvider {
        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return deviceConfigurationService;
        }

        @Override
        public LogBookService logBookService() {
            return logBookService;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return mdcReadingTypeUtilService;
        }

        @Override
        public IssueService issueService() {
            return issueService;
        }

        @Override
        public ManagementBeanFactory managementBeanFactory() {
            return managementBeanFactory;
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return threadPrincipalService;
        }

        @Override
        public UserService userService() {
            return userService;
        }

        @Override
        public NlsService nlsService() {
            return this.engineService().nlsService();
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return protocolPluggableService;
        }

        @Override
        public SocketService socketService() {
            return socketService;
        }

        @Override
        public HexService hexService() {
            return hexService;
        }

        @Override
        public SerialComponentService serialAtComponentService() {
            return serialATComponentService;
        }

        @Override
        public MeteringService meteringService() {
            return meteringService;
        }

        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return engineConfigurationService;
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return connectionTaskService;
        }

        @Override
        public CommunicationTaskService communicationTaskService() {
            return communicationTaskService;
        }

        @Override
        public DeviceService deviceService() {
            return deviceService;
        }

        @Override
        public TopologyService topologyService() {
            return topologyService;
        }

        @Override
        public EngineService engineService() {
            return engineService;
        }

        @Override
        public TransactionService transactionService() {
            return transactionService;
        }

        @Override
        public EventService eventService() {
            return eventService;
        }

        @Override
        public IdentificationService identificationService() {
            return this.engineService().identificationService();
        }

    }

}