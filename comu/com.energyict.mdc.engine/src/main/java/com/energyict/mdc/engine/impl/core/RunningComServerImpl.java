/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.PersistenceException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.IPBasedInboundComPort;
import com.energyict.mdc.common.comserver.InboundCapable;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundCapable;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactory;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactoryImpl;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactory;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactoryImpl;
import com.energyict.mdc.engine.impl.core.logging.ComServerLogger;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.monitor.ComServerOperationalStatisticsImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ServerEventAPIStatistics;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactoryImpl;
import com.energyict.mdc.engine.monitor.ComServerMonitor;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.io.SocketService;

import org.joda.time.DateTimeConstants;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link RunningComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (09:58)
 */
public class RunningComServerImpl implements RunningComServer, Runnable {

    private static final Logger LOGGER = Logger.getLogger(RunningComServerImpl.class.getName());
    /**
     * The default queue size of the DeviceCommandExecutor
     * that is used for non {@link OnlineComServer}s.
     */
    private static final int DEFAULT_STORE_TASK_QUEUE_SIZE = 50;
    /**
     * The default number of threads of the DeviceCommandExecutor
     * that is used for non {@link OnlineComServer}s.
     */
    private static final int DEFAULT_NUMBER_OF_THREADS = 1;
    /**
     * The number of milliseconds that this server will wait for nested
     * {@link ServerProcess}es to complete the shutdown process.
     */
    private static final long SHUTDOWN_WAIT_TIME = 100;
    private final ServiceProvider serviceProvider;
    private final Thesaurus thesaurus;
    private final BlockingQueue<ComPort> forceRefreshQueue = new LinkedBlockingQueue<>();
    private final ComServerDAO comServerDAO;
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private AtomicBoolean continueRunning;
    private Thread self;
    private ScheduledComPortFactory scheduledComPortFactory;
    private ComPortListenerFactory comPortListenerFactory;
    private ThreadFactory threadFactory;
    private ComServer comServer;
    private HighPriorityTaskSchedulerImpl highPriorityTaskScheduler;
    private Map<Long, ScheduledComPort> scheduledComPorts = new ConcurrentHashMap<>();
    private Collection<ComPortListener> comPortListeners = new ArrayList<>();
    private EventMechanism eventMechanism;
    private DeviceCommandExecutorImpl deviceCommandExecutor;
    private TimeOutMonitor timeOutMonitor;
    private ComServerCleanupProcess comTaskExecutionTriggersCleanupProcess;
    private ComServerMonitor operationalMonitor;
    private LoggerHolder loggerHolder;
    private Map<ComPort, List<Long>> comPortPoolsComPortBelongsToCache = new HashMap<>();
    private Map<Long, List<? extends ComPort>> comPortsComPortPoolBelongsTo = new ConcurrentHashMap<>();
    private ComTaskExecFetcher comTaskExecFetcher;

    RunningComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        this.thesaurus = this.getThesaurus(serviceProvider.nlsService());
        this.comServer = comServer;
        registerAsMBean();
        EventPublisher eventPublisher = new EventPublisherImpl(this);
        WebSocketEventPublisherFactoryImpl webSocketEventPublisherFactory =
                new WebSocketEventPublisherFactoryImpl(
                        this,
                        serviceProvider.connectionTaskService(),
                        serviceProvider.communicationTaskService(),
                        serviceProvider.deviceService(),
                        serviceProvider.engineConfigurationService(),
                        serviceProvider.identificationService(),
                        eventPublisher);
        this.eventMechanism = new EventMechanism(eventPublisher, new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory), operationalMonitor.getEventApiStatistics());
        this.comServerDAO = comServerDAO;
        this.initialize(scheduledComPortFactory, comPortListenerFactory, threadFactory);
        this.initializeDeviceCommandExecutor(comServer);
        this.initializeTimeoutMonitor(comServer);
        this.initializeComTaskExecFetcher(comServer);
        this.initializeCleanupProcess(comServer);
        this.addOutboundComPorts(comServer.getOutboundComPorts());
        this.addInboundComPorts(comServer.getInboundComPorts());
    }

    RunningComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, EmbeddedWebServerFactory embeddedWebServerFactory, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        this.thesaurus = this.getThesaurus(serviceProvider.nlsService());
        this.comServer = comServer;
        registerAsMBean();
        this.eventMechanism = new EventMechanism(embeddedWebServerFactory, operationalMonitor.getEventApiStatistics());
        this.comServerDAO = comServerDAO;
        this.initialize(scheduledComPortFactory, comPortListenerFactory, threadFactory);
        this.initializeDeviceCommandExecutor(comServer);
        this.initializeTimeoutMonitor(comServer);
        this.initializeComTaskExecFetcher(comServer);
        this.initializeCleanupProcess(comServer);
        this.addOutboundComPorts(comServer.getOutboundComPorts());
        this.addInboundComPorts(comServer.getInboundComPorts());
    }

    RunningComServerImpl(RemoteComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        this.thesaurus = this.getThesaurus(serviceProvider.nlsService());
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
        registerAsMBean();
        EventPublisher eventPublisher = new EventPublisherImpl(this);
        WebSocketEventPublisherFactoryImpl webSocketEventPublisherFactory =
                new WebSocketEventPublisherFactoryImpl(
                        this,
                        serviceProvider.connectionTaskService(),
                        serviceProvider.communicationTaskService(),
                        serviceProvider.deviceService(),
                        serviceProvider.engineConfigurationService(),
                        serviceProvider.identificationService(),
                        eventPublisher);
        this.eventMechanism = new EventMechanism(eventPublisher, new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory), operationalMonitor.getEventApiStatistics());
        this.initialize(scheduledComPortFactory, comPortListenerFactory, threadFactory);
        this.initializeDeviceCommandExecutor(comServer.getName(), comServer.getServerLogLevel(), DEFAULT_STORE_TASK_QUEUE_SIZE, DEFAULT_NUMBER_OF_THREADS, Thread.NORM_PRIORITY);
        this.initializeTimeoutMonitor(comServer);
        this.initializeComTaskExecFetcher(comServer);
        this.initializeCleanupProcess(comServer);
        this.addOutboundComPorts(comServer.getOutboundComPorts());
        this.addInboundComPorts(comServer.getInboundComPorts());
    }

    RunningComServerImpl(RemoteComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, EmbeddedWebServerFactory embeddedWebServerFactory, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        this.thesaurus = this.getThesaurus(serviceProvider.nlsService());
        this.comServer = comServer;
        registerAsMBean();
        this.eventMechanism = new EventMechanism(embeddedWebServerFactory, operationalMonitor.getEventApiStatistics());
        this.comServerDAO = comServerDAO;
        this.initialize(scheduledComPortFactory, comPortListenerFactory, threadFactory);
        this.initializeDeviceCommandExecutor(comServer.getName(), comServer.getServerLogLevel(), DEFAULT_STORE_TASK_QUEUE_SIZE, DEFAULT_NUMBER_OF_THREADS, Thread.NORM_PRIORITY);
        this.initializeTimeoutMonitor(comServer);
        this.initializeComTaskExecFetcher(comServer);
        this.initializeCleanupProcess(comServer);
        this.addOutboundComPorts(comServer.getOutboundComPorts());
        this.addInboundComPorts(comServer.getInboundComPorts());
    }

    RunningComServerImpl(OfflineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        this.thesaurus = this.getThesaurus(serviceProvider.nlsService());
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
        registerAsMBean();
        EventPublisher eventPublisher = new EventPublisherImpl(this);
        WebSocketEventPublisherFactoryImpl webSocketEventPublisherFactory =
                new WebSocketEventPublisherFactoryImpl(
                        this,
                        serviceProvider.connectionTaskService(),
                        serviceProvider.communicationTaskService(),
                        serviceProvider.deviceService(),
                        serviceProvider.engineConfigurationService(),
                        serviceProvider.identificationService(),
                        eventPublisher);
        this.eventMechanism = new EventMechanism(eventPublisher, new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory), operationalMonitor.getEventApiStatistics());
        this.initialize(scheduledComPortFactory, comPortListenerFactory, threadFactory);
        this.initializeDeviceCommandExecutor(comServer.getName(), comServer.getServerLogLevel(), DEFAULT_STORE_TASK_QUEUE_SIZE, DEFAULT_NUMBER_OF_THREADS, Thread.NORM_PRIORITY);
        this.initializeTimeoutMonitor(comServer);
        this.initializeComTaskExecFetcher(comServer);
        this.initializeCleanupProcess(comServer);
        this.addOutboundComPorts(comServer.getOutboundComPorts());
        this.addInboundComPorts(comServer.getInboundComPorts());
    }

    RunningComServerImpl(OfflineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, EmbeddedWebServerFactory embeddedWebServerFactory, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        this.thesaurus = this.getThesaurus(serviceProvider.nlsService());
        this.comServer = comServer;
        registerAsMBean();
        this.eventMechanism = new EventMechanism(embeddedWebServerFactory, operationalMonitor.getEventApiStatistics());
        this.comServerDAO = comServerDAO;
        this.initialize(scheduledComPortFactory, comPortListenerFactory, threadFactory);
        this.initializeDeviceCommandExecutor(comServer.getName(), comServer.getServerLogLevel(), DEFAULT_STORE_TASK_QUEUE_SIZE, DEFAULT_NUMBER_OF_THREADS, Thread.NORM_PRIORITY);
        this.initializeTimeoutMonitor(comServer);
        this.initializeComTaskExecFetcher(comServer);
        this.initializeCleanupProcess(comServer);
        this.addOutboundComPorts(comServer.getOutboundComPorts());
        this.addInboundComPorts(comServer.getInboundComPorts());
    }

    private Thesaurus getThesaurus(NlsService nlsService) {
        if (nlsService != null) {
            return nlsService.getThesaurus(EngineService.COMPONENTNAME, Layer.DOMAIN);
        }
        return null;
    }

    private void initialize(ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory) {
        this.loggerHolder = new LoggerHolder(comServer);
        this.scheduledComPortFactory = scheduledComPortFactory;
        this.comPortListenerFactory = comPortListenerFactory;
        this.threadFactory = threadFactory;
        // registerAsMBean();
    }

    protected EmbeddedWebServerFactory getEmbeddedWebServerFactory() {
        return this.eventMechanism.embeddedWebServerFactory;
    }

    protected final ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    private void initializeTimeoutMonitor(OutboundCapableComServer comServer) {
        this.timeOutMonitor = new TimeOutMonitorImpl(comServer, this.comServerDAO, this.threadFactory, serviceProvider.threadPrincipalService());
    }

    private void initializeComTaskExecFetcher(OutboundCapableComServer comServer) {
        fillComPortsComPortPoolBelongsTo();
        this.comTaskExecFetcher = new ComTaskExecFetcher(scheduledComPorts, comPortsComPortPoolBelongsTo, threadFactory, comServerDAO, comServer, serviceProvider);
    }

    private void initializeCleanupProcess(OutboundCapableComServer comServer) {
        this.comTaskExecutionTriggersCleanupProcess = new ComServerCleanupProcessImpl(comServer, this.comServerDAO, this.threadFactory);
    }

    private void addOutboundComPorts(List<OutboundComPort> outboundComPorts) {
        outboundComPorts.forEach(this::add);
    }

    private Optional<ScheduledComPort> add(OutboundComPort comPort) {
        ScheduledComPort scheduledComPort = this.getScheduledComPortFactory().newFor(this, comPort);
        if (scheduledComPort == null) {
            return Optional.empty();
        } else {
            this.add(scheduledComPort);
            return Optional.of(scheduledComPort);
        }
    }

    private ScheduledComPortFactory getScheduledComPortFactory() {
        if (this.scheduledComPortFactory == null) {
            this.scheduledComPortFactory =
                    new ScheduledComPortFactoryImpl(
                            this.comServerDAO,
                            this.deviceCommandExecutor,
                            this.getThreadFactory(),
                            new ScheduledComPortFactoryServiceProvider());
        }
        return this.scheduledComPortFactory;
    }

    /**
     * Notifies interested parties that the specified {@link OutboundComPort}
     * is ignored because it is inactive.
     *
     * @param comPort The OutboundComPort that is ignored
     */
    private void ignored(OutboundComPort comPort) {
        this.getLogger().ignoredOutbound(comPort.getName());
    }

    private ScheduledComPort add(ScheduledComPort scheduledComPort) {
        this.scheduledComPorts.put(scheduledComPort.getComPort().getId(), scheduledComPort);
        return scheduledComPort;
    }

    private void addInboundComPorts(List<InboundComPort> inboundComPorts) {
        inboundComPorts.forEach(this::add);
    }

    private Optional<ComPortListener> add(InboundComPort comPort) {
        ComPortListener comPortListener = getComPortListenerFactory().newFor(this, comPort);
        if (comPortListener == null) {
            return Optional.empty();
        } else {
            this.add(comPortListener);
            return Optional.of(comPortListener);
        }
    }

    private void initializeDeviceCommandExecutor(OnlineComServer comServer) {
        this.initializeDeviceCommandExecutor(comServer.getName(), comServer.getServerLogLevel(), comServer.getStoreTaskQueueSize(), comServer.getNumberOfStoreTaskThreads(), comServer.getStoreTaskThreadPriority());
    }

    private void initializeDeviceCommandExecutor(String comServerName, ComServer.LogLevel logLevel, int queueSize, int numberOfThreads, int threadPriority) {
        this.deviceCommandExecutor =
                new DeviceCommandExecutorImpl(
                        comServerName, queueSize, numberOfThreads, threadPriority, logLevel,
                        this.threadFactory,
                        this.serviceProvider.clock(),
                        this.comServerDAO,
                        this.eventMechanism.getEventPublisher(),
                        this.serviceProvider.threadPrincipalService(),
                        this.serviceProvider.deviceMessageService()
                );
    }

    /**
     * Notifies interested parties that the specified {@link InboundComPort}
     * is ignored because it is inactive.
     *
     * @param comPort The InboundComPort that is ignored
     */
    private void ignored(InboundComPort comPort) {
        this.getLogger().ignoredInbound(comPort.getName());
    }

    private ComPortListener add(ComPortListener comPortListener) {
        this.comPortListeners.add(comPortListener);
        return comPortListener;
    }

    @Override
    public ServerProcessStatus getStatus() {
        return this.status;
    }

    @Override
    public final void start() {
        this.status = ServerProcessStatus.STARTING;
        this.continueRunning = new AtomicBoolean(true);
        try {
            this.startComServerDAO();
            this.continueStartupAfterDAOStart();
            this.getLogger().started(this.getComServer());
        } catch (RuntimeException e) {
            this.comServerDAOStartFailed(e);
        }
    }

    @Override
    public final void shutdown() {
        this.shutdown(false);
    }

    @Override
    public void shutdownImmediate() {
        this.shutdown(true);
    }

    protected void startComServerDAO() {
        this.comServerDAO.start();
    }

    private void comServerDAOStartFailed(RuntimeException e) {
        e.printStackTrace(System.err);
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    protected void continueStartupAfterDAOStart() {
        this.startEventMechanism();
        LOGGER.info("Event mechanism started");
        this.startDeviceCommandExecutor();
        LOGGER.info("DeviceCommandExecutor started");
        this.startOutboundComPorts();
        LOGGER.info("OutboundComPorts started");
        this.startInboundComPorts();
        LOGGER.info("InboundComPorts started");
        startHighPriorityTaskScheduler();
        LOGGER.info("High priority task scheduler started");
        this.timeOutMonitor.start();
        LOGGER.info("TimeOutMonitor started");
        this.comTaskExecFetcher.start();
        LOGGER.info("ComTaskExecFetcher started");
        this.comTaskExecutionTriggersCleanupProcess.start();
        LOGGER.info("Cleanup process started");
        self = this.threadFactory.newThread(this);
        self.setName("Changes monitor for " + this.comServer.getName());
        self.start();
        this.installShutdownHooks();
        this.status = ServerProcessStatus.STARTED;
    }

    private void registerAsMBean() {
        if (this.serviceProvider.managementBeanFactory() != null) {
            this.operationalMonitor = (ComServerMonitor) this.serviceProvider.managementBeanFactory().findOrCreateFor(this);
        }
    }

    private void unregisterAsMBean() {
        this.serviceProvider.managementBeanFactory().removeIfExistsFor(this);
    }

    private void installShutdownHooks() {
        this.installVMShutdownHook();
    }

    private void installVMShutdownHook() {
        Thread shutdownHook = this.threadFactory.newThread(this::shutdownImmediate);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void startOutboundComPorts() {
        this.scheduledComPorts.values().forEach(ScheduledComPort::start);
    }

    private void startInboundComPorts() {
        this.comPortListeners.forEach(ComPortListener::start);
    }

    private void startHighPriorityTaskScheduler() {
        if (comServer instanceof OutboundCapableComServer && ((OutboundCapableComServer) comServer).supportsExecutionOfHighPriorityComTasks()) {
            highPriorityTaskScheduler = new HighPriorityTaskSchedulerImpl(this, (OutboundCapableComServer) comServer, serviceProvider, eventMechanism.getEventPublisher());
            highPriorityTaskScheduler.start();
        }
    }

    private void startEventMechanism() {
        this.eventMechanism.start();
    }

    private void startDeviceCommandExecutor() {
        this.deviceCommandExecutor.start();
    }

    private void shutdown(boolean immediate) {
        this.getLogger().shuttingDown(this.getComServer());
        this.doShutdown();
        this.shutdownNestedServerProcesses(immediate);
        if (!immediate) {
            this.awaitNestedServerProcessesAreShutDown();
        }
        this.unregisterAsMBean();
        Services.objectMapperService(null);
        this.getLogger().shutDownComplete(this.getComServer());
    }

    protected void shutdownNestedServerProcesses(boolean immediate) {
        shutdownComServerDAO(immediate);
        shutdownEventMechanism(immediate);
        shutdownTimeOutMonitor(immediate);
        shutdownComTaskExecFetcher(immediate);
        shutdownCleanupProcess(immediate);
        shutdownOutboundComPorts(immediate);
        shutdownInboundComPorts(immediate);
        shutdownPriorityScheduler(immediate);
        shutdownDeviceCommandExecutor(immediate);
    }

    private void shutdownComServerDAO(boolean immediate) {
        if (immediate) {
            this.comServerDAO.shutdownImmediate();
        } else {
            this.comServerDAO.shutdown();
        }
    }

    private void shutdownEventMechanism(boolean immediate) {
        if (this.eventMechanism != null) {
            this.eventMechanism.shutdown(immediate);
        }
    }

    private void doShutdown() {
        this.status = ServerProcessStatus.SHUTTINGDOWN;
        this.continueRunning.set(false);
        self.interrupt();   // in case the thread was sleeping between detecting changes
    }

    private void shutdownTimeOutMonitor(boolean immediate) {
        if (immediate) {
            this.timeOutMonitor.shutdownImmediate();
        } else {
            this.timeOutMonitor.shutdown();
        }
    }

    private void shutdownComTaskExecFetcher(boolean immediate) {
        if (immediate) {
            this.comTaskExecFetcher.shutdownImmediate();
        } else {
            this.comTaskExecFetcher.shutdown();
        }
    }

    private void shutdownCleanupProcess(boolean immediate) {
        if (immediate) {
            this.comTaskExecutionTriggersCleanupProcess.shutdownImmediate();
        } else {
            this.comTaskExecutionTriggersCleanupProcess.shutdown();
        }
    }

    private void awaitNestedServerProcessesAreShutDown() {
        while (this.atLeastOneNestedServerProcessStillRunning()) {
            try {
                Thread.sleep(SHUTDOWN_WAIT_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean atLeastOneNestedServerProcessStillRunning() {
        for (ServerProcess serverProcess : this.getNestedServerProcesses()) {
            if (!ServerProcessStatus.SHUTDOWN.equals(serverProcess.getStatus())) {
                return true;
            }
        }
        return false;
    }

    protected List<ServerProcess> getNestedServerProcesses() {
        List<ServerProcess> nestedProcesses = new ArrayList<>();
        nestedProcesses.addAll(this.scheduledComPorts.values());
        nestedProcesses.addAll(this.comPortListeners);
        nestedProcesses.add(this.deviceCommandExecutor);
        return nestedProcesses;
    }

    private void shutdownOutboundComPorts(boolean immediate) {
        for (ScheduledComPort scheduledComPort : this.scheduledComPorts.values()) {
            if (immediate) {
                scheduledComPort.shutdownImmediate();
            } else {
                scheduledComPort.shutdown();
            }
        }
    }

    private void shutdownInboundComPorts(boolean immediate) {
        for (ComPortListener comPortListener : this.comPortListeners) {
            if (immediate) {
                comPortListener.shutdownImmediate();
            } else {
                comPortListener.shutdown();
            }
        }
    }

    private void shutdownPriorityScheduler(boolean immediate) {
        if (highPriorityTaskScheduler != null) {
            if (immediate) {
                highPriorityTaskScheduler.shutdownImmediate();
            } else {
                highPriorityTaskScheduler.shutdown();
            }
        }
    }

    private void shutdownDeviceCommandExecutor(boolean immediate) {
        if (immediate) {
            this.deviceCommandExecutor.shutdownImmediate();
        } else {
            this.deviceCommandExecutor.shutdown();
        }
    }

    @Override
    public void run() {
        while (continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            monitorChanges();
        }
        status = ServerProcessStatus.SHUTDOWN;
    }

    private void monitorChanges() {
        try {
            Thread.sleep(getChangesInterPollDelayMillis());
            getLogger().monitoringChanges(this.getComServer());
            ComServer newVersion = comServerDAO.refreshComServer(comServer);
            operationalMonitor.getOperationalStatistics().setLastCheckForChangesTimestamp(Date.from(serviceProvider.clock().instant()));
            if (newVersion == null) {
                // ComServer was deleted or made obsolete, shutdown
                shutdown();
            } else if (newVersion == comServer) {
                // No changes found
            } else {
                if (!newVersion.isActive()) {
                    // ComServer is no longer active, shutdown
                    shutdown();
                } else {
                    if (newVersion instanceof OutboundCapableComServer) {
                        shutdownTimeOutMonitor(true);
                    }
                    comPortPoolsComPortBelongsToCache = new HashMap<>(); // Flush the cache
                    resetLoggerHolder(newVersion);
                    applyChanges((InboundCapable) newVersion);
                    applyChanges((OutboundCapable) newVersion);
                    applyDelayChanges(newVersion);
                    notifyChangesApplied();
                    String oldComServerName = comServer.getName();
                    comServer = newVersion;
                    if (!oldComServerName.equals(newVersion.getName())) {
                        // Name changed, notify the ManagementBeanFactory
                        serviceProvider.managementBeanFactory().renamed(oldComServerName, this);
                    }
                    fillComPortsComPortPoolBelongsTo();
                    serviceProvider.engineConfigurationService().findContainingComPortPoolsForComServer(comServer);
                    if (newVersion instanceof OutboundCapableComServer) {
                        initializeTimeoutMonitor((OutboundCapableComServer) comServer);
                        timeOutMonitor.start();
                    }
                }
            }
        } catch (UnderlyingSQLFailedException e) {
            getLogger().monitorChangesFailed(comServer, e.getCause());
        } catch (InterruptedException e) {
            getLogger().comServerExecutionInterrupted(comServer, e.getMessage());
            Thread.currentThread().interrupt();
        } catch (PersistenceException e) {
            getLogger().databaseErrorOccurredDuringUpdatingChanges(comServer, e.getMessage());
            // DatabaseExceptions are caught and swallowed in ComPorts:
            //      ScheduledComPortImpl#run
            //      ComPortListenerImpl#run
        } catch (Throwable e) {
            getLogger().otherComServerException(comServer, e.getMessage());
        }
    }

    private void fillComPortsComPortPoolBelongsTo() {
        comPortsComPortPoolBelongsTo.clear();
        comPortsComPortPoolBelongsTo.putAll(
                serviceProvider.engineConfigurationService().findContainingComPortPoolsForComServer(comServer)
                        .stream().filter(ComPortPool::isActive)
                        .filter(comPortPool -> !comPortPool.isInbound())
                        .collect(Collectors.toConcurrentMap(k -> k.getId(),
                                v -> v.getComPorts().stream()
                                        .filter(comPort -> comPort.getComServer().getId() == comServer.getId())
                                        .collect(Collectors.toList()))));

    }

    private void resetLoggerHolder(ComServer newVersion) {
        this.loggerHolder.reset(newVersion);
    }

    private void applyDelayChanges(ComServer newVersion) {
        if (!this.comServer.getSchedulingInterPollDelay().equals(newVersion.getSchedulingInterPollDelay())) {
            this.notifySchedulingInterPollDelayChange(newVersion.getSchedulingInterPollDelay());
        }
        if (!this.comServer.getChangesInterPollDelay().equals(newVersion.getChangesInterPollDelay())) {
            this.notifyChangesInterPollDelayChange(newVersion.getChangesInterPollDelay());
        }
    }

    private void notifySchedulingInterPollDelayChange(TimeDuration schedulingInterPollDelay) {
        for (ScheduledComPort comPort : this.scheduledComPorts.values()) {
            comPort.schedulingInterpollDelayChanged(schedulingInterPollDelay);
        }
        for (ComPortListener comPort : this.comPortListeners) {
            comPort.schedulingInterpollDelayChanged(schedulingInterPollDelay);
        }
        ComServerMonitor monitor = this.getOperationalMonitor();
        ((ComServerOperationalStatisticsImpl) monitor.getOperationalStatistics()).setSchedulingInterpollDelay(schedulingInterPollDelay);
    }

    private void notifyChangesInterPollDelayChange(TimeDuration changesInterPollDelay) {
        for (ScheduledComPort comPort : this.scheduledComPorts.values()) {
            comPort.changesInterpollDelayChanged(changesInterPollDelay);
        }
        for (ComPortListener comPort : this.comPortListeners) {
            comPort.changesInterpollDelayChanged(changesInterPollDelay);
        }
        ComServerMonitor monitor = this.getOperationalMonitor();
        ((ComServerOperationalStatisticsImpl) monitor.getOperationalStatistics()).setChangesInterpollDelay(changesInterPollDelay);
    }

    private void applyChanges(InboundCapable newVersion) {
        this.shutdownAndRemoveOldInboundComPorts(newVersion);
        this.restartChangedInboundComPorts(newVersion);
        this.addAndStartNewInboundComPorts(newVersion);
    }

    protected void notifyChangesApplied() {
        // Notify interested parties that changes have been applied
    }

    private void applyChanges(OutboundCapable newVersion) {
        this.shutdownAndRemoveOldOutboundComPorts(newVersion);
        this.refreshChangedOutboundComPorts(newVersion);
        this.addAndStartNewOutboundComPorts(newVersion);
        if (newVersion instanceof OnlineComServer) {
            OnlineComServer onlineComServer = (OnlineComServer) newVersion;
            this.applyDeviceCommandExecutorChanges(onlineComServer);
        }
    }

    private void applyDeviceCommandExecutorChanges(OnlineComServer newVersion) {
        this.deviceCommandExecutor.changeQueueCapacity(newVersion.getStoreTaskQueueSize());
        this.deviceCommandExecutor.changeNumberOfThreads(newVersion.getNumberOfStoreTaskThreads());
        this.deviceCommandExecutor.changeThreadPriority(newVersion.getStoreTaskThreadPriority());
    }

    private void restartChangedInboundComPorts(InboundCapable newVersion) {
        for (InboundComPort changedComPort : this.changedInboundComPortsIn(newVersion)) {
            this.getListenerFor(changedComPort).ifPresent(this::shutdownAndRemove);
            this.add(changedComPort).ifPresent(ComPortListener::start);
        }
    }

    private void shutdownAndRemove(ComPortListener oldListener) {
        oldListener.shutdown();
        this.comPortListeners.remove(oldListener);
    }

    private void addAndStartNewInboundComPorts(InboundCapable newVersion) {
        for (InboundComPort comPort : this.newActivatedInboundComPortsIn(newVersion)) {
            Optional<ComPortListener> comPortListener = this.add(comPort);
            if (comPortListener.isPresent()) {
                comPortListener.get().start();
            } else {
                this.ignored(comPort);
            }
        }
    }

    /**
     * Returns the existing {@link InboundComPort}s that have changed in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of changed InboundComPorts
     */
    private Collection<InboundComPort> changedInboundComPortsIn(InboundCapable newVersion) {
        List<InboundComPort> newVersionInboundComPorts = newVersion.getInboundComPorts();
        Collection<InboundComPort> changed = new ArrayList<>(newVersionInboundComPorts.size());    // at most all ComPorts in the new version can have changed
        for (InboundComPort inboundComPort : newVersionInboundComPorts) {
            Optional<ComPortListener> existingListener = this.getListenerFor(inboundComPort);
            if (existingListener.isPresent() && this.hasChanged(existingListener.get().getComPort(), inboundComPort)) {
                changed.add(inboundComPort);
            }
        }
        this.getLogger().inboundComPortChangesDetected(changed.size());
        return changed;
    }

    private boolean hasChanged(InboundComPort existingComPort, InboundComPort newVersion) {
        return (existingComPort instanceof ModemBasedInboundComPort)
                || this.hasChanged((IPBasedInboundComPort) existingComPort, (IPBasedInboundComPort) newVersion);
    }

    private boolean hasChanged(IPBasedInboundComPort existingComPort, IPBasedInboundComPort newVersion) {
        return existingComPort.getPortNumber() != newVersion.getPortNumber()
                || existingComPort.getNumberOfSimultaneousConnections() != newVersion.getNumberOfSimultaneousConnections()
                || !existingComPort.getName().equals(newVersion.getName());
    }

    /**
     * Returns the newly activated {@link InboundComPort}s that can be found in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of newly activated InboundComPorts
     */
    private Collection<InboundComPort> newActivatedInboundComPortsIn(InboundCapable newVersion) {
        List<InboundComPort> newVersionInboundComPorts = newVersion.getInboundComPorts();
        Collection<InboundComPort> newlyActivated =
                newVersionInboundComPorts
                        .stream()
                        .filter(inboundComPort -> inboundComPort.isActive() && !this.isAlreadyActive(inboundComPort))
                        .collect(Collectors.toList());
        this.getLogger().newInboundComPortsDetected(newlyActivated.size());
        return newlyActivated;
    }

    private void shutdownAndRemoveOldInboundComPorts(InboundCapable newVersion) {
        for (InboundComPort comPort : this.deactivatedInboundComPortsIn(newVersion)) {
            this.getListenerFor(comPort).ifPresent(this::shutdownAndRemove);
        }
        List<Long> actualComPortIds =
                newVersion.getInboundComPorts()
                        .stream()
                        .map(InboundComPort::getId)
                        .collect(Collectors.toList());
        Iterator<ComPortListener> comPortListenerIterator = this.comPortListeners.iterator();
        while (comPortListenerIterator.hasNext()) {
            ComPortListener comPortListener = comPortListenerIterator.next();
            if (!actualComPortIds.contains(comPortListener.getComPort().getId())) {
                comPortListenerIterator.remove();
                comPortListener.shutdown();
            }
        }
    }

    /**
     * Returns the {@link InboundComPort}s that have been deactivated in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of deactivated InboundComPorts
     */
    private Collection<InboundComPort> deactivatedInboundComPortsIn(InboundCapable newVersion) {
        List<InboundComPort> newVersionInboundComPorts = newVersion.getInboundComPorts();
        Collection<InboundComPort> deactivated =
                newVersionInboundComPorts
                        .stream()
                        .filter(inboundComPort -> this.isAlreadyActive(inboundComPort) && !inboundComPort.isActive())
                        .collect(Collectors.toList());
        this.getLogger().inboundComPortsDeactivated(deactivated.size());
        return deactivated;
    }

    private Optional<ComPortListener> getListenerFor(InboundComPort comPort) {
        for (ComPortListener listener : this.comPortListeners) {
            if (comPort.getId() == listener.getComPort().getId()) {
                return Optional.of(listener);
            }
        }
        return Optional.empty();
    }

    private boolean isAlreadyActive(InboundComPort comPort) {
        return this.getListenerFor(comPort).isPresent();
    }

    private void restartChangedOutboundComPorts(OutboundCapable newVersion) {
        for (OutboundComPort changedComPort : this.changedOutboundComPortsIn(newVersion)) {
            this.getSchedulerFor(changedComPort).ifPresent(this::shutdownAndRemove);
            this.add(changedComPort).ifPresent(ScheduledComPort::start);
        }
    }

    private void refreshChangedOutboundComPorts(OutboundCapable newVersion) {
        for (OutboundComPort changedComPort : this.changedOutboundComPortsIn(newVersion)) {
            this.getSchedulerFor(changedComPort).ifPresent(scp -> scp.reload(changedComPort));
        }
    }

    private void shutdownAndRemove(ScheduledComPort previouslyScheduledComPort) {
        previouslyScheduledComPort.shutdown();
        this.scheduledComPorts.remove(previouslyScheduledComPort);
    }

    private void addAndStartNewOutboundComPorts(OutboundCapable newVersion) {
        for (OutboundComPort comPort : this.newActivatedOutboundComPortsIn(newVersion)) {
            Optional<ScheduledComPort> scheduledComPort = this.add(comPort);
            if (scheduledComPort.isPresent()) {
                scheduledComPort.get().start();
            } else {
                this.ignored(comPort);
            }
        }
    }

    /**
     * Returns the existing {@link OutboundComPort}s that have changed in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of changed OutboundComPorts
     */
    private Collection<OutboundComPort> changedOutboundComPortsIn(OutboundCapable newVersion) {
        List<OutboundComPort> newVersionOutboundComPorts = newVersion.getOutboundComPorts();
        Collection<OutboundComPort> changed = new ArrayList<>(newVersionOutboundComPorts.size());    // at most all ComPorts in the new version can have changed
        for (OutboundComPort outboundComPort : newVersionOutboundComPorts) {
            Optional<ScheduledComPort> existingScheduledComPort = this.getSchedulerFor(outboundComPort);
            if (existingScheduledComPort.isPresent() && this.hasChanged(existingScheduledComPort.get().getComPort(), outboundComPort)) {
                changed.add(outboundComPort);
            }
        }
        this.getLogger().outboundComPortChangesDetected(changed.size());
        return changed;
    }

    private boolean hasChanged(OutboundComPort existingComPort, OutboundComPort newVersion) {
        return existingComPort.getNumberOfSimultaneousConnections() != newVersion.getNumberOfSimultaneousConnections()
                || !existingComPort.getName().equals(newVersion.getName());
    }

    /**
     * Returns the newly activated {@link OutboundComPort}s that can be found in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of newly activated OutboundComPorts
     */
    private Collection<OutboundComPort> newActivatedOutboundComPortsIn(OutboundCapable newVersion) {
        List<OutboundComPort> newVersionOutboundComPorts = newVersion.getOutboundComPorts();
        Collection<OutboundComPort> newlyActivated =
                newVersionOutboundComPorts
                        .stream()
                        .filter(outboundComPort -> outboundComPort.isActive() && !this.isAlreadyActive(outboundComPort))
                        .collect(Collectors.toList());
        this.getLogger().newOutboundComPortsDetected(newlyActivated.size());
        return newlyActivated;
    }

    private void shutdownAndRemoveOldOutboundComPorts(OutboundCapable newVersion) {
        for (OutboundComPort comPort : this.deactivatedOutboundComPortsIn(newVersion)) {
            this.getSchedulerFor(comPort).ifPresent(this::shutdownAndRemove);
        }
        List<Long> actualComPortIds =
                newVersion.getOutboundComPorts()
                        .stream()
                        .map(OutboundComPort::getId)
                        .collect(Collectors.toList());
        Iterator<ScheduledComPort> scheduledComPortIterator = this.scheduledComPorts.values().iterator();
        while (scheduledComPortIterator.hasNext()) {
            ScheduledComPort scheduledComPort = scheduledComPortIterator.next();
            if (!actualComPortIds.contains(scheduledComPort.getComPort().getId())) {
                scheduledComPortIterator.remove();
                scheduledComPort.shutdown();
            }
        }
    }

    /**
     * Returns the {@link OutboundComPort}s that have been deactivated in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of deactivated OutboundComPorts
     */
    private Collection<OutboundComPort> deactivatedOutboundComPortsIn(OutboundCapable newVersion) {
        List<OutboundComPort> newVersionOutboundComPorts = newVersion.getOutboundComPorts();
        Collection<OutboundComPort> deactivated =
                newVersionOutboundComPorts
                        .stream()
                        .filter(outboundComPort -> this.isAlreadyActive(outboundComPort) && !outboundComPort.isActive())
                        .collect(Collectors.toList());
        this.getLogger().outboundComPortsDeactivated(deactivated.size());
        return deactivated;
    }

    private Optional<ScheduledComPort> getSchedulerFor(OutboundComPort comPort) {
        for (ScheduledComPort scheduledComPort : this.scheduledComPorts.values()) {
            if (comPort.getId() == scheduledComPort.getComPort().getId()) {
                return Optional.of(scheduledComPort);
            }
        }
        return Optional.empty();
    }

    private boolean isAlreadyActive(OutboundComPort comPort) {
        return this.getSchedulerFor(comPort).isPresent();
    }

    private int getChangesInterPollDelayMillis() {
        return this.comServer.getChangesInterPollDelay().getSeconds() * DateTimeConstants.MILLIS_PER_SECOND;
    }

    private long getSchedulingInterPollDelayMillis() {
        return this.comServer.getSchedulingInterPollDelay().getSeconds() * DateTimeConstants.MILLIS_PER_SECOND;
    }

    private ComPortListenerFactory getComPortListenerFactory() {
        if (this.comPortListenerFactory == null) {
            this.comPortListenerFactory =
                    new ComPortListenerFactoryImpl(
                            this.comServerDAO,
                            this.deviceCommandExecutor,
                            new ComPortListenerServiceProvider());
        }
        return this.comPortListenerFactory;
    }

    @Override
    public ComServer getComServer() {
        return this.comServer;
    }

    public ComServerDAO getComServerDAO() {
        return comServerDAO;
    }

    @Override
    public boolean isRemoteQueryApiStarted() {
        return false;
    }

    @Override
    public int getCollectedDataStorageCapacity() {
        return this.deviceCommandExecutor.getCapacity();
    }

    @Override
    public int getCurrentCollectedDataStorageSize() {
        return this.deviceCommandExecutor.getCurrentSize();
    }

    @Override
    public int getCurrentCollectedDataStorageLoadPercentage() {
        return this.deviceCommandExecutor.getCurrentLoadPercentage();
    }

    @Override
    public int getNumberOfCollectedDataStorageThreads() {
        return this.deviceCommandExecutor.getNumberOfThreads();
    }

    @Override
    public int getCollectedDataStorageThreadPriority() {
        return this.deviceCommandExecutor.getThreadPriority();
    }

    @Override
    public String getAcquiredTokenThreadNames() {
        return this.deviceCommandExecutor.getAcquiredTokenThreadNames();
    }

    @Override
    public void eventClientRegistered() {
        ComServerMonitor monitor = this.getOperationalMonitor();
        if (monitor != null) {
            monitor.getEventApiStatistics().clientRegistered();
        }
    }

    @Override
    public void eventClientUnregistered() {
        ComServerMonitor monitor = this.getOperationalMonitor();
        monitor.getEventApiStatistics().clientUnregistered();
    }

    @Override
    public void eventWasPublished() {
        ComServerMonitor monitor = this.getOperationalMonitor();
        if (monitor != null) {
            ((ServerEventAPIStatistics) monitor.getEventApiStatistics()).eventWasPublished();
        }
    }

    @Override
    public void refresh(ComPort comPort) {
        if (comPort != null) {
            getLogger().comPortScheduledForRefresh(comPort.getName());
        } else {
            getLogger().comPortScheduledForRefresh("null");
        }
        this.forceRefreshQueue.add(comPort);
    }

    @Override
    public boolean canExecuteTasksWithHighPriority() {
        synchronized (scheduledComPorts) {
            return !scheduledComPorts.isEmpty();
        }
    }

    public Map<Long, Integer> getHighPriorityLoadPerComPortPool() {
        synchronized (scheduledComPorts) {
            Map<Long, Integer> highPriorityLoadPerComPortPool = new HashMap<>();
            for (ScheduledComPort scheduledComPort : scheduledComPorts.values()) {
                Map<Long, Integer> loadPerPoolMap = scheduledComPort.getHighPriorityLoadPerComPortPool();
                for (Map.Entry<Long, Integer> loadPerPool : loadPerPoolMap.entrySet()) {
                    if (highPriorityLoadPerComPortPool.containsKey(loadPerPool.getKey())) {
                        highPriorityLoadPerComPortPool.put(
                                loadPerPool.getKey(),
                                highPriorityLoadPerComPortPool.get(loadPerPool.getKey()) + loadPerPool.getValue()
                        );
                    } else {
                        highPriorityLoadPerComPortPool.put(loadPerPool.getKey(), loadPerPool.getValue());
                    }
                }
            }
            return highPriorityLoadPerComPortPool;
        }
    }

    @Override
    public void executeWithHighPriority(HighPriorityComJob job) {
        synchronized (scheduledComPorts) {
            List<ScheduledComPort> interruptCandidates = findInterruptCandidates(scheduledComPorts.values(), job.getConnectionTask().getComPortPool());
            if (interruptCandidates.isEmpty()) {
                throw new UnableToAcceptHighPriorityTasksException();
            }
            Collections.sort(interruptCandidates, new ScheduledComPortForInterruptionComparator(job));
            interruptCandidates.get(0).executeWithHighPriority(job);
        }
    }

    @Override
    public void restartAllComPorts() {
        this.scheduledComPorts.values().forEach(ScheduledComPort::shutdown);
        this.scheduledComPorts.values().forEach(ScheduledComPort::start);
    }

    protected ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    private ComServerLogger getLogger() {
        return this.loggerHolder.get();
    }

    ComServerMonitor getOperationalMonitor() {
        return this.operationalMonitor;
    }

    private ArrayList<ScheduledComPort> findInterruptCandidates(Collection<ScheduledComPort> scheduledComPorts, ComPortPool comPortPool) {
        ArrayList<ScheduledComPort> candidates = new ArrayList<>(scheduledComPorts.size());
        for (ScheduledComPort scheduledComPort : scheduledComPorts) {
            if (getContainingComPortPools(scheduledComPort).contains(comPortPool.getId())) {
                Integer highPriorityLoad = scheduledComPort.getHighPriorityLoadPerComPortPool().get(comPortPool.getId());
                if (highPriorityLoad == null || highPriorityLoad < scheduledComPort.getThreadCount()) {
                    candidates.add(scheduledComPort);
                } // else the ScheduledComPort is already busy with the execution of a high priority task(s) - it doesn't make sense to interrupt a thread of such port
            }
        }
        return candidates;
    }

    private List<Long> getContainingComPortPools(ScheduledComPort scheduledComPort) {
        if (!comPortPoolsComPortBelongsToCache.containsKey(scheduledComPort.getComPort())) {
            comPortPoolsComPortBelongsToCache.put(scheduledComPort.getComPort(), getComServerDAO().findContainingActiveComPortPoolsForComPort(scheduledComPort.getComPort()));
        }
        return comPortPoolsComPortBelongsToCache.get(scheduledComPort.getComPort());
    }

    public interface ServiceProvider extends ComServerDAOImpl.ServiceProvider {

        IssueService issueService();

        ManagementBeanFactory managementBeanFactory();

        ThreadPrincipalService threadPrincipalService();

        NlsService nlsService();

        ProtocolPluggableService protocolPluggableService();

        MdcReadingTypeUtilService mdcReadingTypeUtilService();

        UserService userService();

        FirmwareService firmwareService();

        DeviceConfigurationService deviceConfigurationService();

        SocketService socketService();

        HexService hexService();

        SerialComponentService serialAtComponentService();

        MeteringService meteringService();

        TimeOfUseCampaignService touService();

    }

    private static class ScheduledComPortForInterruptionComparator implements Comparator<ScheduledComPort> {
        private final HighPriorityComJob job;

        private ScheduledComPortForInterruptionComparator(HighPriorityComJob job) {
            super();
            this.job = job;
        }

        @Override
        public int compare(ScheduledComPort first, ScheduledComPort second) {
            if (first.isExecutingOneOf(job.getComTaskExecutions())) {
                // The second comport cannot be executing the same task because of the locking mechanism
                return -1;
            } else if (second.isExecutingOneOf(job.getComTaskExecutions())) {
                return 1;
            } else {
                // Neither of the two comports are executing the very same ComTaskExecution
                if (job.getConnectionTask().getNumberOfSimultaneousConnections() > 1) {
                    /* The connection can be opened in parallel so we can connect from another comport.
                     * Therefore, it does not really matter which of the two we pick.
                     * Since we will be interrupting one of the two comports,
                     * we will interrupt the one with the highest number of free threads. */
                    return compareOnNumberOfFreeThreads(first, second);
                } else {
                    // Only one of the two tasks can be connected to the task
                    if (first.isConnectedTo(job.getConnectionTask())) {
                        return -1;
                    } else if (second.isConnectedTo(job.getConnectionTask())) {
                        return 1;
                    }
                    /* None of the two are connected so it does not really matter
                     * which of the two we pick. Since we will be interrupting comports,
                     * we will interrupt the one with the highest number of free threads. */
                    return compareOnNumberOfFreeThreads(first, second);
                }
            }
        }

        private int compareOnNumberOfFreeThreads(ScheduledComPort first, ScheduledComPort second) {
            return Integer.compare(getFreeThreadCount(second), getFreeThreadCount(first));  // We want the one with the highest free thread count
        }

        private int getFreeThreadCount(ScheduledComPort comPort) {
            return comPort.getThreadCount() - comPort.getActiveThreadCount();
        }
    }

    private class EventMechanism {
        private final EmbeddedWebServerFactory embeddedWebServerFactory;
        private final EventPublisher eventPublisher;
        private EmbeddedWebServer embeddedWebServer;

        private EventMechanism(EmbeddedWebServerFactory embeddedWebServerFactory, EventAPIStatistics eventAPIStatistics) {
            this(new EventPublisherImpl(RunningComServerImpl.this), embeddedWebServerFactory, eventAPIStatistics);
        }

        private EventMechanism(EventPublisher eventPublisher, EmbeddedWebServerFactory embeddedWebServerFactory, EventAPIStatistics eventAPIStatistics) {
            super();
            this.eventPublisher = eventPublisher;
            this.embeddedWebServerFactory = embeddedWebServerFactory;
            this.embeddedWebServer = this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer, eventAPIStatistics);
        }

        private EmbeddedWebServerFactory getEmbeddedWebServerFactory() {
            return embeddedWebServerFactory;
        }

        private EventPublisher getEventPublisher() {
            return eventPublisher;
        }

        private void start() {
            embeddedWebServer.start();
        }

        private void shutdown(boolean immediate) {
            eventPublisher.shutdown();
            if (immediate) {
                embeddedWebServer.shutdownImmediate();
            } else {
                embeddedWebServer.shutdown();
            }
        }

    }

    private class LoggerHolder {
        private ComServer comServer;
        private ComServerLogger logger;
        private LogLevel lastLogLevel;

        private LoggerHolder(ComServer comServer) {
            super();
            this.comServer = comServer;
        }

        private ComServerLogger get() {
            if (logger == null || logLevelChanged()) {
                lastLogLevel = getServerLogLevel(comServer);
                logger = newLogger(lastLogLevel);
            }
            return logger;
        }

        private boolean logLevelChanged() {
            return !lastLogLevel.equals(getServerLogLevel(comServer));
        }

        private ComServerLogger newLogger(LogLevel logLevel) {
            return LoggerFactory.getLoggerFor(ComServerLogger.class, logLevel);
        }

        private LogLevel getServerLogLevel(ComServer comServer) {
            return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
        }

        public void reset(ComServer newVersion) {
            comServer = newVersion;
            logger = null;
            lastLogLevel = null;
        }

    }

    private class ComPortListenerServiceProvider implements ComChannelBasedComPortListenerImpl.ServiceProvider {

        @Override
        public SocketService socketService() {
            return serviceProvider.socketService();
        }

        @Override
        public ComServerDAO comServerDAO() {
            return comServerDAO;
        }

        @Override
        public ThreadFactory threadFactory() {
            return threadFactory;
        }

        @Override
        public InboundComPortConnectorFactory inboundComPortConnectorFactory() {
            return new InboundComPortConnectorFactoryImpl(
                    serviceProvider.serialAtComponentService(),
                    serviceProvider.socketService(),
                    serviceProvider.hexService(),
                    eventMechanism.eventPublisher,
                    serviceProvider.clock(),
                    serviceProvider.deviceMessageService()
            );
        }

        @Override
        public EmbeddedWebServerFactory embeddedWebServerFactory() {
            return eventMechanism.getEmbeddedWebServerFactory();
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return serviceProvider.protocolPluggableService();
        }

        @Override
        public DeviceMessageSpecificationService deviceMessageSpecificationService() {
            return serviceProvider.deviceMessageSpecificationService();
        }

        @Override
        public EventPublisher eventPublisher() {
            return eventMechanism.getEventPublisher();
        }

        @Override
        public UserService userService() {
            return serviceProvider.userService();
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return serviceProvider.threadPrincipalService();
        }

        @Override
        public ManagementBeanFactory managementBeanFactory() {
            return serviceProvider.managementBeanFactory();
        }

        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return serviceProvider.deviceConfigurationService();
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return serviceProvider.connectionTaskService();
        }

        @Override
        public Thesaurus thesaurus() {
            return RunningComServerImpl.this.thesaurus;
        }

        @Override
        public DeviceService deviceService() {
            return serviceProvider.deviceService();
        }

        @Override
        public TopologyService topologyService() {
            return serviceProvider.topologyService();
        }

        @Override
        public EngineService engineService() {
            return serviceProvider.engineService();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public HexService hexService() {
            return serviceProvider.hexService();
        }

        @Override
        public TransactionService transactionService() {
            return serviceProvider.transactionService();
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
        public MeteringService meteringService() {
            return serviceProvider.meteringService();
        }

        @Override
        public FirmwareService firmwareService() {
            return serviceProvider.firmwareService();
        }

        @Override
        public TimeOfUseCampaignService touService() {
            return serviceProvider.touService();
        }

        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public NlsService nlsService() {
            return serviceProvider.nlsService();
        }

        @Override
        public IssueService issueService() {
            return serviceProvider.issueService();
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return serviceProvider.deviceMessageService();
        }
    }

    private class ScheduledComPortFactoryServiceProvider implements ScheduledComPortImpl.ServiceProvider {
        @Override
        public UserService userService() {
            return serviceProvider.userService();
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return serviceProvider.threadPrincipalService();
        }

        @Override
        public ManagementBeanFactory managementBeanFactory() {
            return serviceProvider.managementBeanFactory();
        }

        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return serviceProvider.deviceConfigurationService();
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return serviceProvider.connectionTaskService();
        }

        @Override
        public Thesaurus thesaurus() {
            return RunningComServerImpl.this.thesaurus;
        }

        @Override
        public DeviceService deviceService() {
            return serviceProvider.deviceService();
        }

        @Override
        public TopologyService topologyService() {
            return serviceProvider.topologyService();
        }

        @Override
        public EngineService engineService() {
            return serviceProvider.engineService();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public HexService hexService() {
            return serviceProvider.hexService();
        }

        @Override
        public TransactionService transactionService() {
            return serviceProvider.transactionService();
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
        public MeteringService meteringService() {
            return serviceProvider.meteringService();
        }

        public FirmwareService firmwareService() {
            return serviceProvider.firmwareService();
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return serviceProvider.protocolPluggableService();
        }

        @Override
        public TimeOfUseCampaignService touService() {
            return serviceProvider.touService();
        }

        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public NlsService nlsService() {
            return serviceProvider.nlsService();
        }

        @Override
        public IssueService issueService() {
            return serviceProvider.issueService();
        }

        @Override
        public EventPublisher eventPublisher() {
            return eventMechanism.getEventPublisher();
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return serviceProvider.deviceMessageService();
        }

    }
}