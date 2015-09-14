package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.Counters;
import com.energyict.mdc.engine.impl.core.InboundJobExecutionDataProcessor;
import com.energyict.mdc.engine.impl.core.InboundJobExecutionGroup;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.UnknownInboundDeviceEvent;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionCompletionEvent;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionStartedEvent;
import com.energyict.mdc.engine.impl.events.connection.CloseConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.EstablishConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.UndiscoveredCloseConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.UndiscoveredEstablishConnectionEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.protocol.inbound.statistics.StatisticsMonitoringHttpServletRequest;
import com.energyict.mdc.engine.impl.protocol.inbound.statistics.StatisticsMonitoringHttpServletResponse;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.FindMultipleDevices;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.StopWatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the inbound communication process,
 * covering all of the validation aspect (i.e. when is communication allowed)
 * and the delegation to an actual {@link DeviceProtocol}
 * if that is necessary.
 * It is assumed that the {@link InboundDeviceProtocol} that is provided
 * has already been initialized, i.e. the proper init methods has already been called.
 * Which method needs to be called will depend on how the communication data
 * is obtained {@link InboundDeviceProtocol.InputDataType}.
 * All events, either success or failure is communicated to the Device via the
 * {@link InboundDeviceProtocol#provideResponse(InboundDeviceProtocol.DiscoverResponseType)
 * InboundDeviceProtocol.provideResponse}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (14:11)
 */
public class InboundCommunicationHandler {

    public interface ServiceProvider extends JobExecution.ServiceProvider {

        EmbeddedWebServerFactory embeddedWebServerFactory();

        ProtocolPluggableService protocolPluggableService();

        EventPublisher eventPublisher();

        UserService userService();

        ThreadPrincipalService threadPrincipalService();

    }

    private static final long NANOS_IN_MILLI = 1000000L;

    private final ServiceProvider serviceProvider;

    private InboundComPort comPort;
    private ComServerDAO comServerDAO;
    private DeviceCommandExecutor deviceCommandExecutor;
    private List<ComTaskExecution> deviceComTaskExecutions;
    private InboundConnectionTask connectionTask;
    private InboundDiscoveryContextImpl context;
    private InboundDeviceProtocol.DiscoverResponseType responseType;
    private StopWatch discovering;
    private CompositeComPortDiscoveryLogger logger;

    public InboundCommunicationHandler(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super();
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
    }

    private class OfflineDeviceServiceProvider implements OfflineDeviceImpl.ServiceProvider {

        @Override
        public TopologyService topologyService() {
            return serviceProvider.topologyService();
        }

        @Override
        public Optional<DeviceCache> findProtocolCacheByDevice(Device device) {
            return serviceProvider.engineService().findDeviceCacheByDevice(device);
        }

        @Override
        public IdentificationService identificationService() {
            return serviceProvider.identificationService();
        }

    }

    /**
     * Handles all of the business and technical aspects of inbound communication.
     *
     * @param inboundDeviceProtocol The InboundDeviceProtocol that will discover which {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
     *                              started the inbound communication session
     * @param context               The InboundDiscoveryContext
     */
    public void handle(InboundDeviceProtocol inboundDeviceProtocol, InboundDiscoveryContextImpl context) {
        this.publish(new UndiscoveredEstablishConnectionEvent(new ComServerEventServiceProvider(), this.comPort));
        this.initializeContext(context);
        this.initializeLogging();
        Optional<OfflineDevice> device;
        InboundDeviceProtocol.DiscoverResultType discoverResultType;
        try {
            discoverResultType = this.doDiscovery(inboundDeviceProtocol);
            this.publishDiscoveryResult(discoverResultType, inboundDeviceProtocol);
            device = this.comServerDAO.findOfflineDevice(inboundDeviceProtocol.getDeviceIdentifier());
            if (device.isPresent()) {
                this.logger.deviceIdentified(inboundDeviceProtocol.getDeviceIdentifier(), this.getComPort());
                this.handleKnownDevice(inboundDeviceProtocol, context, discoverResultType, device.get());
            }
            else {
                this.handleUnknownDevice(inboundDeviceProtocol);
            }
        } catch (Throwable e) {
            this.handleRuntimeExceptionDuringDiscovery(inboundDeviceProtocol, e);
        }
        this.closeContext();
    }

    private void publishDiscoveryResult(InboundDeviceProtocol.DiscoverResultType discoverResultType, InboundDeviceProtocol inboundDeviceProtocol) {
        switch (discoverResultType) {
            case IDENTIFIER: {
                this.logger.discoveryFoundIdentifierOnly(inboundDeviceProtocol.getDeviceIdentifier(), this.getComPort());
                break;
            }
            case DATA: {
                this.logger.discoveryFoundIdentifierAndData(inboundDeviceProtocol.getDeviceIdentifier(), this.getComPort());
                break;
            }
            default: {
                throw new CommunicationException(MessageSeeds.UNSUPPORTED_DISCOVERY_RESULT_TYPE, discoverResultType);
            }
        }
    }

    /**
     * For each Device we found, we will create a failing InboundComSession so it is clear that the
     * connection was not properly setup because an error occurred during discovery.
     *
     * @param inboundDeviceProtocol the inboundDeviceProtocol
     * @param t                     the exception
     */
    private void handleRuntimeExceptionDuringDiscovery(InboundDeviceProtocol inboundDeviceProtocol, Throwable t) {
        this.responseType = InboundDeviceProtocol.DiscoverResponseType.FAILURE;
        if (inboundDeviceProtocol.getDeviceIdentifier() != null) {
            List<? extends BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister>> allDevices = getAllPossiblyRelatedDevices(inboundDeviceProtocol);
            if (allDevices.size() > 1) {
                this.responseType = InboundDeviceProtocol.DiscoverResponseType.DUPLICATE_DEVICE;
            } else if (allDevices.size() == 0) {
                this.responseType = InboundDeviceProtocol.DiscoverResponseType.DEVICE_NOT_FOUND;
            }
            allDevices.stream().filter(device -> deviceIsReadyForInboundCommunicationOnThisPort(new OfflineDeviceImpl((Device) device, new DeviceOfflineFlags(), new OfflineDeviceServiceProvider()))).forEach(device -> {
                List<DeviceCommandExecutionToken> tokens = this.deviceCommandExecutor.tryAcquireTokens(1);
                if (!tokens.isEmpty() && this.connectionTask != null) {
                    CompositeDeviceCommand storeCommand = new ComSessionRootDeviceCommand();
                    storeCommand.add(createFailedInboundComSessionDeviceCommand(this.responseType.equals(InboundDeviceProtocol.DiscoverResponseType.DUPLICATE_DEVICE) ?
                            createDuplicateSerialNumberComSessionBuilder(device.getSerialNumber()) : createErrorComSessionBuilder(t)));
                    this.deviceCommandExecutor.execute(storeCommand, tokens.get(0));
                } else {
                    this.responseType = InboundDeviceProtocol.DiscoverResponseType.SERVER_BUSY;
                }
            });
        }
        provideResponse(inboundDeviceProtocol, this.responseType);
    }

    private ComSessionBuilder createErrorComSessionBuilder(Throwable t) {
        return serviceProvider.connectionTaskService().
                buildComSession(connectionTask, comPort.getComPortPool(), comPort, now()).
                addJournalEntry(now(), ComServer.LogLevel.ERROR, t);
    }

    private ComSessionBuilder createDuplicateSerialNumberComSessionBuilder(String serialNumber) {
        return serviceProvider.connectionTaskService().
                buildComSession(connectionTask, comPort.getComPortPool(), comPort, now()).
                addJournalEntry(now(), ComServer.LogLevel.ERROR,
                        getThesaurus().getFormat(MessageSeeds.INBOUND_DUPLICATE_SERIALNUMBER_FAILURE).format(serialNumber));
    }

    private Thesaurus getThesaurus() {
        return serviceProvider.thesaurus();
    }

    private List<? extends BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister>> getAllPossiblyRelatedDevices(InboundDeviceProtocol inboundDeviceProtocol) {
        List<? extends BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister>> allDevices;
        if (FindMultipleDevices.class.isAssignableFrom(inboundDeviceProtocol.getDeviceIdentifier().getClass())) {
            FindMultipleDevices<?> multipleDevicesFinder = (FindMultipleDevices<?>) inboundDeviceProtocol.getDeviceIdentifier();
            allDevices = multipleDevicesFinder.getAllDevices();
        } else {
            allDevices = Collections.singletonList(inboundDeviceProtocol.getDeviceIdentifier().findDevice());
        }
        return allDevices;
    }

    /**
     * This will create an {@link CreateInboundComSession} which holds a ComSessionShadow with successIndicator
     * {@link ComSession.SuccessIndicator#SetupError} and a ComSessionJournalEntryShadow which
     * contains the detailed exception.
     *
     * @param comSessionBuilder to use
     * @return the CreateInboundComSession
     */
    private CreateInboundComSession createFailedInboundComSessionDeviceCommand(ComSessionBuilder comSessionBuilder) {
        return new CreateInboundComSession(getComPort(), this.connectionTask, comSessionBuilder, ComSession.SuccessIndicator.SetupError, serviceProvider.clock());
    }

    private void handleUnknownDevice(InboundDeviceProtocol inboundDeviceProtocol) {
        List<InboundDeviceProtocolPluggableClass> classes = serviceProvider.protocolPluggableService().findInboundDeviceProtocolPluggableClassByClassName(inboundDeviceProtocol.getClass().getName());
        if (!classes.isEmpty()) {
            UnknownInboundDeviceEvent event = new UnknownInboundDeviceEvent(this.comPort, inboundDeviceProtocol.getDeviceIdentifier(), classes.get(0));
            this.comServerDAO.signalEvent(EventType.UNKNOWN_INBOUND_DEVICE.topic(), event);
        }
        // Todo: do something for the DoS attacks?
        this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.DEVICE_NOT_FOUND);
    }

    private void handleKnownDevice(InboundDeviceProtocol inboundDeviceProtocol, InboundDiscoveryContext context, InboundDeviceProtocol.DiscoverResultType discoverResultType, OfflineDevice device) {
        Cryptographer cryptographer = context.getCryptographer();
        if (this.deviceRequiresEncryption(device) && cryptographer != null && !cryptographer.wasUsed()) {
            this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.ENCRYPTION_REQUIRED);
        } else {
            if (this.deviceIsReadyForInboundCommunicationOnThisPort(device)) {
                this.startDeviceSessionInContext();
                this.handleDeviceReadyForInboundCommunicationOnThisPort(inboundDeviceProtocol, discoverResultType, device);
            } else {
                this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.DEVICE_DOES_NOT_EXPECT_INBOUND);
            }
        }
    }

    private void handleDeviceReadyForInboundCommunicationOnThisPort(InboundDeviceProtocol inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResultType discoverResultType, OfflineDevice offlineDevice) {
        List<DeviceCommandExecutionToken> tokens = this.deviceCommandExecutor.tryAcquireTokens(1);
        if (tokens.isEmpty()) {
            this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.SERVER_BUSY);
        } else {
            DeviceCommandExecutionToken singleToken = tokens.get(0);
            if (InboundDeviceProtocol.DiscoverResultType.IDENTIFIER.equals(discoverResultType)) {
                this.provideResponse(inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType.SUCCESS);
                this.handOverToDeviceProtocol(singleToken);
            } else {
                //Note that the provideResponse method is called in the storing service. Depending on the result of the storing, either success or failure is returned.
                this.processCollectedData(inboundDeviceProtocol, singleToken, offlineDevice);
            }
        }
    }

    private void startDeviceSessionInContext() {
        this.publish(new EstablishConnectionEvent(new ComServerEventServiceProvider(), this.comPort, this.connectionTask));
        context.buildComSession(connectionTask, comPort.getComPortPool(), comPort, now());
    }

    public InboundComPort getComPort() {
        return comPort;
    }

    public InboundConnectionTask getConnectionTask() {
        return connectionTask;
    }

    public List<ComTaskExecution> getDeviceComTaskExecutions() {
        return deviceComTaskExecutions;
    }

    public InboundDiscoveryContextImpl getContext() {
        return context;
    }

    private InboundDeviceProtocol.DiscoverResultType doDiscovery(InboundDeviceProtocol inboundDeviceProtocol) {
        this.logger.discoveryStarted(inboundDeviceProtocol.getClass().getName(), this.getComPort());
        this.discovering = new StopWatch();
        try {
            return inboundDeviceProtocol.doDiscovery();
        } finally {
            this.discovering.stop();
        }
    }

    private void initializeLogging() {
        this.logger = new CompositeComPortDiscoveryLogger(this.newNormalLogger(), this.newEventLogger());
    }

    private ComPortDiscoveryLogger newNormalLogger() {
        ComPortDiscoveryLogger logger = LoggerFactory.getUniqueLoggerFor(ComPortDiscoveryLogger.class, this.getServerLogLevel());
        Logger actualLogger = ((LoggerFactory.LoggerHolder) logger).getLogger();
        actualLogger.addHandler(new DiscoveryContextLogHandler(this.serviceProvider.clock(), this.context));
        this.context.setLogger(actualLogger);
        return logger;

    }

    private ComPortDiscoveryLogger newEventLogger() {
        return LoggerFactory.getLoggerFor(ComPortDiscoveryLogger.class, this.getAnonymousLogger());
    }

    private Logger getAnonymousLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(new ComPortDiscoveryLogHandler(this, this.serviceProvider.eventPublisher(), new ComServerEventServiceProvider()));
        return logger;
    }

    private void initializeContext(InboundDiscoveryContextImpl context) {
        this.context = context;
        if (this.inWebContext()) {
            HttpServletRequest request = context.getServletRequest();
            HttpServletResponse response = context.getServletResponse();
            context.setServletRequest(new StatisticsMonitoringHttpServletRequest(request));
            context.setServletResponse(new StatisticsMonitoringHttpServletResponse(response));
        }
    }

    private boolean inWebContext() {
        return this.context.getServletRequest() != null;
    }

    private void closeContext() {
        if (this.connectionTask != null) {
            this.publish(
                    new CloseConnectionEvent(
                            new ComServerEventServiceProvider(),
                            this.comPort,
                            this.connectionTask));
        } else {
            this.publish(
                    new UndiscoveredCloseConnectionEvent(
                            new ComServerEventServiceProvider(),
                            this.comPort));
        }
    }

    public void complete() {
        this.appendStatisticalInformationToComSession();
    }

    private void appendStatisticalInformationToComSession() {
        ComSessionBuilder comSessionBuilder = this.context.getComSessionBuilder();
        if (comSessionBuilder != null) {
            comSessionBuilder.connectDuration(Duration.ofMillis(0));
            if (this.inWebContext()) {
                StatisticsMonitoringHttpServletRequest request = this.getMonitoringRequest();
                StatisticsMonitoringHttpServletResponse response = this.getMonitoringResponse();
                long discoverMillis = this.discovering.getElapsed() / NANOS_IN_MILLI;
                long talkMillis = discoverMillis + request.getTalkTime() + response.getTalkTime();
                comSessionBuilder.talkDuration(Duration.ofMillis(talkMillis));
                comSessionBuilder.addReceivedBytes(request.getBytesRead()).addSentBytes(response.getBytesSent());
                comSessionBuilder.addReceivedPackets(1).addSentPackets(1);
            } else {
                ComPortRelatedComChannel comChannel = this.getComChannel();
                long discoverMillis = this.discovering.getElapsed() / NANOS_IN_MILLI;
                comSessionBuilder.talkDuration(Duration.ofMillis(discoverMillis).plus(comChannel.talkTime()));
                Counters sessionCounters = comChannel.getSessionCounters();
                comSessionBuilder.addReceivedBytes(sessionCounters.getBytesRead());
                comSessionBuilder.addSentBytes(sessionCounters.getBytesSent());
                comSessionBuilder.addReceivedPackets(sessionCounters.getPacketsRead());
                comSessionBuilder.addSentPackets(sessionCounters.getPacketsSent());
            }
        } else {
            // Todo: deal with the unknown device situation
        }
    }

    private StatisticsMonitoringHttpServletRequest getMonitoringRequest() {
        return (StatisticsMonitoringHttpServletRequest) this.context.getServletRequest();
    }

    private StatisticsMonitoringHttpServletResponse getMonitoringResponse() {
        return (StatisticsMonitoringHttpServletResponse) this.context.getServletResponse();
    }

    private ComPortRelatedComChannel getComChannel() {
        return this.context.getComChannel();
    }

    public ComSession.SuccessIndicator getFailureIndicator() {
        switch (this.responseType) {
            case SUCCESS: {
                assert false : "if-test that was supposed to verify that the discovery response type was NOT success clearly failed";
                throw CodingException.unrecognizedEnumValue(this.responseType, com.energyict.mdc.engine.impl.MessageSeeds.UNRECOGNIZED_ENUM_VALUE);
            }
            case DATA_ONLY_PARTIALLY_HANDLED: {
                // Should not be marked as failed, but as success (the logging will mention part of the data is not handled/stored)
                return ComSession.SuccessIndicator.Success;
            }
            case DEVICE_DOES_NOT_EXPECT_INBOUND:
            case DEVICE_NOT_FOUND:
            case DUPLICATE_DEVICE:
            case ENCRYPTION_REQUIRED: {
                return ComSession.SuccessIndicator.SetupError;
            }

            case FAILURE:
            case STORING_FAILURE:
            case SERVER_BUSY: {
                return ComSession.SuccessIndicator.Broken;
            }
            default: {
                throw CodingException.unrecognizedEnumValue(this.responseType, com.energyict.mdc.engine.impl.MessageSeeds.UNRECOGNIZED_ENUM_VALUE);
            }
        }
    }

    public void provideResponse(InboundDeviceProtocol inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType responseType) {
        this.publishResponse(inboundDeviceProtocol, responseType);
        inboundDeviceProtocol.provideResponse(responseType);
        this.responseType = responseType;
    }

    private void publishResponse(InboundDeviceProtocol inboundDeviceProtocol, InboundDeviceProtocol.DiscoverResponseType responseType) {
        switch (responseType) {
            case DEVICE_NOT_FOUND: {
                this.logger.deviceNotFound(inboundDeviceProtocol.getDeviceIdentifier(), this.getComPort());
                break;
            }
            case DUPLICATE_DEVICE: {
                this.logger.discoveryFailed(inboundDeviceProtocol.getClass().getName(), this.getComPort());
                break;
            }
            case FAILURE: {
                this.logger.discoveryFailed(inboundDeviceProtocol.getClass().getName(), this.getComPort());
                break;
            }
            case STORING_FAILURE: {
                //Storing error was already logged in the DeviceCommandExecutor
                break;
            }
            case DEVICE_DOES_NOT_EXPECT_INBOUND: {
                this.logger.deviceNotConfiguredForInboundCommunication(inboundDeviceProtocol.getDeviceIdentifier(), this.getComPort());
                break;
            }
            case ENCRYPTION_REQUIRED: {
                this.logger.deviceRequiresEncryptedData(inboundDeviceProtocol.getDeviceIdentifier(), this.getComPort());
                break;
            }
            case SERVER_BUSY: {
                this.logger.serverTooBusy(inboundDeviceProtocol.getDeviceIdentifier(), this.getComPort());
                break;
            }
            case SUCCESS: {
                // should already be logged
                break;
            }
            default: {
            }
        }
    }

    public InboundDeviceProtocol.DiscoverResponseType getResponseType() {
        return responseType;
    }

    private boolean deviceRequiresEncryption(OfflineDevice device) {
        // Todo: Use one of the typed properties so may need to delegate this to the protocol
        return false;
    }

    protected void handOverToDeviceProtocol(DeviceCommandExecutionToken token) {
        InboundJobExecutionGroup inboundJobExecutionGroup =
                new InboundJobExecutionGroup(
                        getComPort(),
                        comServerDAO,
                        deviceCommandExecutor,
                        getContext(),
                        this.serviceProvider,
                        this);
        inboundJobExecutionGroup.setToken(token);
        inboundJobExecutionGroup.setConnectionTask(this.connectionTask);
        inboundJobExecutionGroup.executeDeviceProtocol(this.deviceComTaskExecutions);
    }

    protected void processCollectedData(InboundDeviceProtocol inboundDeviceProtocol, DeviceCommandExecutionToken token, OfflineDevice offlineDevice) {
        this.publishComTaskExecutionStartedEvents();
        InboundJobExecutionDataProcessor inboundJobExecutionDataProcessor =
                new InboundJobExecutionDataProcessor(
                        getComPort(),
                        comServerDAO,
                        deviceCommandExecutor,
                        getContext(),
                        inboundDeviceProtocol,
                        offlineDevice,
                        this.serviceProvider,
                        this, logger);
        inboundJobExecutionDataProcessor.setToken(token);
        inboundJobExecutionDataProcessor.setConnectionTask(this.connectionTask);
        inboundJobExecutionDataProcessor.executeDeviceProtocol(this.deviceComTaskExecutions);
        this.publishComTaskExecutionCompletedEvents();
    }

    private void publishComTaskExecutionStartedEvents() {
        for (ComTaskExecution comTaskExecution : this.deviceComTaskExecutions) {
            this.publish(
                    new ComTaskExecutionStartedEvent(
                            new ComServerEventServiceProvider(),
                            comTaskExecution,
                            this.comPort,
                            this.connectionTask));
        }
    }

    private void publishComTaskExecutionCompletedEvents() {
        for (ComTaskExecution comTaskExecution : this.deviceComTaskExecutions) {
            this.publish(
                    new ComTaskExecutionCompletionEvent(
                            new ComServerEventServiceProvider(),
                            comTaskExecution,
                            ComTaskExecutionSession.SuccessIndicator.Success,
                            this.comPort,
                            this.connectionTask));
        }
    }

    private boolean deviceIsReadyForInboundCommunicationOnThisPort(OfflineDevice device) {
        this.deviceComTaskExecutions = this.comServerDAO.findExecutableInboundComTasks(device, this.comPort);
        if (this.deviceComTaskExecutions.isEmpty()) {
            this.connectionTask = null;
            return false;
        } else {
            // There is at most one InboundConnectionTask for every device
            this.connectionTask = (InboundConnectionTask) this.deviceComTaskExecutions.get(0).getConnectionTask().orElse(null);
            return true;
        }
    }

    private Instant now() {
        return serviceProvider.clock().instant();
    }

    protected LogLevel getServerLogLevel() {
        return this.getServerLogLevel(this.getComPort());
    }

    private LogLevel getServerLogLevel(ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel(ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
    }

    private void publish(ComServerEvent event) {
        this.serviceProvider.eventPublisher().publish(event);
    }

    private class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }
    }

    /**
     * Provides an implementation for the {@link ComPortDiscoveryLogger} interface
     * that is actually a compisite of multiple loggers
     * and simply delegates to each of the separate loggers.
     */
    private class CompositeComPortDiscoveryLogger implements ComPortDiscoveryLogger {
        private List<ComPortDiscoveryLogger> loggers;

        private CompositeComPortDiscoveryLogger(ComPortDiscoveryLogger... loggers) {
            super();
            this.loggers = Arrays.asList(loggers);
        }

        @Override
        public void discoveryStarted(String discoveryProtocolClassName, InboundComPort comPort) {
            this.loggers.forEach(each -> each.discoveryStarted(discoveryProtocolClassName, comPort));
        }

        @Override
        public void discoveryFailed(String discoveryProtocolClassName, InboundComPort comPort) {
            this.loggers.forEach(each -> each.discoveryFailed(discoveryProtocolClassName, comPort));
        }

        @Override
        public void discoveryFoundIdentifierOnly(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
            this.loggers.forEach(each -> each.discoveryFoundIdentifierOnly(deviceIdentifier, comPort));
        }

        @Override
        public void discoveryFoundIdentifierAndData(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
            this.loggers.forEach(each -> each.discoveryFoundIdentifierAndData(deviceIdentifier, comPort));
        }

        @Override
        public void deviceNotFound(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
            this.loggers.forEach(each -> each.deviceNotFound(deviceIdentifier, comPort));
        }

        @Override
        public void deviceNotConfiguredForInboundCommunication(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
            this.loggers.forEach(each -> each.deviceNotConfiguredForInboundCommunication(deviceIdentifier, comPort));
        }

        @Override
        public void deviceRequiresEncryptedData(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
            this.loggers.forEach(each -> each.deviceRequiresEncryptedData(deviceIdentifier, comPort));
        }

        @Override
        public void serverTooBusy(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
            this.loggers.forEach(each -> each.serverTooBusy(deviceIdentifier, comPort));
        }

        @Override
        public void deviceIdentified(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
            this.loggers.forEach(each -> each.deviceIdentified(deviceIdentifier, comPort));
        }

        @Override
        public void collectedDataWasFiltered(String dataType, DeviceIdentifier deviceIdentifier, ComPort comPort) {
            this.loggers.forEach(each -> each.collectedDataWasFiltered(dataType, deviceIdentifier, comPort));
        }

    }

}