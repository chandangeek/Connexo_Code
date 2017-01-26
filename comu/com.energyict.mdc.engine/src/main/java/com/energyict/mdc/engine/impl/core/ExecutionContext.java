package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.CreateOutboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandFactoryImpl;
import com.energyict.mdc.engine.impl.commands.store.PublishConnectionSetupFailureEvent;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommand;
import com.energyict.mdc.engine.impl.core.events.ComPortCommunicationLogHandler;
import com.energyict.mdc.engine.impl.core.logging.ComPortConnectionLogger;
import com.energyict.mdc.engine.impl.core.logging.CompositeComPortConnectionLogger;
import com.energyict.mdc.engine.impl.core.logging.CompositeLogger;
import com.energyict.mdc.engine.impl.core.logging.ExecutionContextLogHandler;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionCompletionEvent;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionFailureEvent;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionStartedEvent;
import com.energyict.mdc.engine.impl.events.connection.CannotEstablishConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.CloseConnectionEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.meterdata.DeviceCommandFactory;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.protocol.exceptions.ConnectionException;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 19/05/2014
 * Time: 16:12
 */
public final class ExecutionContext implements JournalEntryFactory {

    private static final long NANOS_IN_MILLI = 1000000L;
    private final ServiceProvider serviceProvider;
    private final JobExecution jobExecution;
    public StopWatch connecting;
    public StopWatch executing;
    public CompositeComPortConnectionLogger connectionLogger;
    private boolean connectionFailed;
    private ConnectionTaskPropertyCache connectionTaskPropertyCache = new ConnectionTaskPropertyCache();
    private DeviceCommandFactory deviceCommandFactory;
    private ComPortRelatedComChannel comPortRelatedComChannel;
    private ComSessionBuilder sessionBuilder;
    private Optional<ComTaskExecutionSessionBuilder> currentTaskExecutionBuilder = Optional.empty();
    private ComCommandJournalist journalist;
    private ComPort comPort;
    private ConnectionTask<?, ?> connectionTask;
    private ComTaskExecution comTaskExecution;
    private CompositeLogger logger;
    private ComTaskExecutionComCommand comTaskExecutionComCommand;
    private CompositeDeviceCommand storeCommand;

    public ExecutionContext(JobExecution jobExecution, ConnectionTask<?, ?> connectionTask, ComPort comPort, boolean logConnectionProperties, ServiceProvider serviceProvider) {
        super();
        this.jobExecution = jobExecution;
        this.comPort = comPort;
        this.connectionTask = connectionTask;
        this.serviceProvider = serviceProvider;
        this.sessionBuilder = serviceProvider.connectionTaskService().buildComSession(this.connectionTask, this.connectionTask.getComPortPool(), this.comPort, serviceProvider.clock().instant());
        if (logConnectionProperties && this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
            this.addConnectionPropertiesAsJournalEntries();
        }
        this.initializeLogging();
    }

    private void initializeLogging() {
        this.logger = new CompositeLogger();
        this.connectionLogger = new CompositeComPortConnectionLogger();
        this.addNormalLogger();
        this.addEventLogger();
    }

    private ComServerDAO getComServerDAO() {
        return this.jobExecution.getComServerDAO();
    }

    private void addNormalLogger() {
        ComPortConnectionLogger normalLogger = LoggerFactory.getUniqueLoggerFor(ComPortConnectionLogger.class, this.getCommunicationLogLevel(this.comPort));
        Logger actualLogger = ((LoggerFactory.LoggerHolder) normalLogger).getLogger();
        actualLogger.addHandler(new ExecutionContextLogHandler(this.serviceProvider.clock(), this));
        this.connectionLogger.add(normalLogger);
        this.logger.add(actualLogger);
    }

    private LogLevel getCommunicationLogLevel(ComPort comPort) {
        return this.getCommunicationLogLevel(comPort.getComServer());
    }

    private LogLevel getCommunicationLogLevel(ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getCommunicationLogLevel());
    }

    private void addEventLogger() {
        //ComPortOperationsLoggingEvent
        Logger actualLogger = this.getAnonymousLogger(new ComPortCommunicationLogHandler(this.getComPort(), this.serviceProvider.eventPublisher(), new ComServerEventServiceProvider()));
        ComPortConnectionLogger eventLogger = LoggerFactory.getLoggerFor(ComPortConnectionLogger.class, actualLogger);
        this.connectionLogger.add(eventLogger);
        this.logger.add(actualLogger);
    }

    private Logger getAnonymousLogger(Handler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(handler);
        return logger;
    }

    public void close() {
        if (!this.isConnected()) {
            return; // Establishing the connection didn't succeed, so nothing to close
        }

        try {
            this.comPortRelatedComChannel.close();
            this.publish(new CloseConnectionEvent(new ComServerEventServiceProvider(), this.getComPort(), this.getConnectionTask()));
        } finally {
            jobExecution.appendStatisticalInformationToComSession();
        }
    }

    public Clock clock() {
        return this.serviceProvider.clock();
    }

    public DeviceMessageService deviceMessageService() {
        return this.serviceProvider.deviceMessageService();
    }

    public EventPublisher eventPublisher() {
        return this.serviceProvider.eventPublisher();
    }

    /**
     * Perform a logical connect if this has not been done yet.
     * Return true if the connect succeeded, false otherwise.
     *
     * @return true if the connect succeeded, false otherwise
     */
    public boolean connect() {
        this.connectionFailed = false;
        this.connecting = new StopWatch();
        this.executing = new StopWatch();
        this.executing.stop();  // Do not auto start but start it manually as soon as execution starts.
        this.connectionTask = this.getComServerDAO().executionStarted(this.connectionTask, this.comPort.getComServer());
        try {
            this.setComPortRelatedComChannel(this.jobExecution.findOrCreateComChannel(this.connectionTaskPropertyCache));
            return this.jobExecution.isConnected();
        } catch (ConnectionException e) {
            this.comPortRelatedComChannel = null;
            this.connectionFailed(e, this.connectionTask);
        } finally {
            if (this.isConnected()) {
                this.connecting.stop();
            }
        }
        return false;
    }

    protected Duration getElapsedTimeInMillis() {
        return Duration.ofMillis(this.connecting.getElapsed() / NANOS_IN_MILLI);
    }

    private boolean isConnected() {
        return this.comPortRelatedComChannel != null;
    }

    private void addConnectionPropertiesAsJournalEntries() {
        List<ConnectionTaskProperty> connectionProperties = this.getConnectionTaskProperties();
        this.connectionTaskPropertyCache.setProperties(connectionProperties);
        if (!connectionProperties.isEmpty()) {
            StringBuilder builder = new StringBuilder("Connection properties: ");
            Holder<String> separator = HolderBuilder.first("").andThen(", ");
            for (ConnectionTaskProperty connectionProperty : connectionProperties) {
                builder.append(separator.get());
                this.appendPropertyToMessage(builder, connectionProperty);
            }
            sessionBuilder.addJournalEntry(now(), ComServer.LogLevel.INFO, builder.toString());
        }
    }

    private void logConnectionSetupFailure(Throwable t) {
        getComSessionBuilder().addJournalEntry(now(), LogLevelMapper.forComServerLogLevel().fromLogLevel(getCommunicationLogLevel(getComPort())), "Connection setup error", t);
    }

    private void appendPropertyToMessage(StringBuilder builder, ConnectionTaskProperty property) {
        this.appendPropertyToMessage(builder, property.getName(), property.getValue());
    }

    private void addProtocolDialectPropertiesAsJournalEntries(ComTaskExecution comTaskExecution) {
        TypedProperties protocolDialectTypedProperties = JobExecution.getProtocolDialectTypedProperties(comTaskExecution);
        if (!protocolDialectTypedProperties.propertyNames().isEmpty()) {
            currentTaskExecutionBuilder.ifPresent(b -> b.addComTaskExecutionMessageJournalEntry(now(), ComServer.LogLevel.INFO, asString(protocolDialectTypedProperties), ""));
        }
    }

    private void appendPropertyToMessage(StringBuilder builder, String propertyName, Object propertyValue) {
        builder.append(
                MessageFormat.format(
                        "{0} = {1}",
                        propertyName, String.valueOf(propertyValue))
        );
    }

    private String asString(TypedProperties protocolDialectTypedProperties) {
        StringBuilder builder = new StringBuilder("Protocol dialect properties: ");
        Holder<String> separator = HolderBuilder.first("").andThen(", ");
        for (String propertyName : protocolDialectTypedProperties.propertyNames()) {
            builder.append(separator.get());
            this.appendPropertyToMessage(builder, propertyName, protocolDialectTypedProperties.getProperty(propertyName));
        }
        return builder.toString();
    }

    /**
     * Tests if the LogLevel is enabled,
     * in which case something should be logged in the ComSession.
     *
     * @param logLevel The ComServer.LogLevel
     * @return A flag that indicates if the DeviceCommand should be logged
     */
    private boolean isLogLevelEnabled(ComServer.LogLevel logLevel) {
        return this.comPort.getComServer().getCommunicationLogLevel().compareTo(logLevel) >= 0;
    }

    public void createJournalEntry(ComServer.LogLevel logLevel, String message) {
        if (this.currentTaskExecutionBuilder.isPresent()) {
            this.createComTaskExecutionMessageJournalEntry(logLevel, message);
        } else {
            this.createComSessionJournalEntry(logLevel, message);
        }
    }

    private void createComTaskExecutionMessageJournalEntry(ComServer.LogLevel logLevel, String message) {
        this.currentTaskExecutionBuilder.ifPresent(b -> b.addComTaskExecutionMessageJournalEntry(now(), logLevel, message, ""));
    }

    private void createComSessionJournalEntry(ComServer.LogLevel logLevel, String message) {
        getComSessionBuilder().addJournalEntry(now(), logLevel, message);
    }

    /**
     * Handles the fact that the connection to the device failed.
     * <ul>
     * <li>Set start, stop time</li>
     * <li>Set connect, talk, store and total time</li>
     * <li>Clean the session statistics</li>
     * </ul>
     * Note that the Start date is set while initializing and
     * the stop date is set in the close method.
     *
     * @param e              The ConnectionException that explains the failure
     * @param connectionTask The ConnectionTask that failed to connect
     * @see #close()
     */
    private void connectionFailed(ConnectionException e, ConnectionTask<?, ?> connectionTask) {
        this.connectionFailed = true;

        try {
            this.zeroOutTimings();
            this.cleanStatistics();
        } finally {

            this.getStoreCommand().add(
                    new PublishConnectionSetupFailureEvent(
                            connectionTask,
                            this.comPort,
                            this.jobExecution.getComTaskExecutions(),
                            getDeviceCommandServiceProvider()));

            this.connectionLogger.cannotEstablishConnection(e, this.getJob().getThreadName());
            this.publish(new CannotEstablishConnectionEvent(new ComServerEventServiceProvider(), this.getComPort(), connectionTask, e));
            logConnectionSetupFailure(e);
        }
    }

    private void zeroOutTimings() {
        getComSessionBuilder().connectDuration(Duration.ZERO);
        getComSessionBuilder().talkDuration(Duration.ZERO);
        getComSessionBuilder().storeDuration(Duration.ZERO);
    }

    private void cleanStatistics() {
        getComSessionBuilder().resetSentBytes();
        getComSessionBuilder().resetReceivedBytes();
        getComSessionBuilder().resetReceivedPackets();
        getComSessionBuilder().resetSentPackets();
    }

    public JobExecution getJob() {
        return this.jobExecution;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        if (this.logger == null) {
            this.logger = new CompositeLogger();
        }
        this.logger.add(logger);
    }

    public ComPort getComPort() {
        return comPort;
    }

    public ConnectionTask<?, ?> getConnectionTask() {
        return connectionTask;
    }

    public ComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }

    public ComPortRelatedComChannel getComPortRelatedComChannel() {
        return comPortRelatedComChannel;
    }

    private void setComPortRelatedComChannel(ComPortRelatedComChannel comPortRelatedComChannel) {
        if (comPortRelatedComChannel != null) { // Is null for inbound communication via servlet technology
            comPortRelatedComChannel.setJournalEntryFactory(this);
            this.comPortRelatedComChannel = comPortRelatedComChannel;
            this.jobExecution.connected(comPortRelatedComChannel);
        }
    }

    public ComSessionBuilder getComSessionBuilder() {
        return sessionBuilder;
    }

    public void setComSessionBuilder(ComSessionBuilder comSessionBuilder) {
        this.sessionBuilder = comSessionBuilder;
    }

    public Optional<ComTaskExecutionSessionBuilder> getCurrentTaskExecutionBuilder() {
        return currentTaskExecutionBuilder;
    }

    public void createComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription) {
        this.currentTaskExecutionBuilder.ifPresent(b -> b.addComCommandJournalEntry(timestamp, completionCode, errorDesciption, commandDescription));
    }

    /**
     * Indicates we are going to start the execution of the ComTaskExecutionComCommand.
     * It is the time and place for the executionContext to prepare itself for this command
     *
     * @param comTaskExecutionComCommand the command that is going to be executed
     */
    public void start(ComTaskExecutionComCommand comTaskExecutionComCommand) {
        getComServerDAO().executionStarted(comTaskExecutionComCommand.getComTaskExecution(), getComPort(), true);
        this.publish(
                new ComTaskExecutionStartedEvent(
                        new ComServerEventServiceProvider(),
                        comTaskExecutionComCommand.getComTaskExecution(),
                        this.getComPort(),
                        this.getConnectionTask()
                ));
        this.comTaskExecution = comTaskExecutionComCommand.getComTaskExecution();
        connectionLogger.startingTask(Thread.currentThread().getName(), comTaskExecution.getComTask().getName());
        if (this.isConnected()) {
            executionStopWatchStart();
            Counters taskSessionCounters = this.comPortRelatedComChannel.getTaskSessionCounters();
            taskSessionCounters.resetBytesRead();
            taskSessionCounters.resetBytesSent();
            taskSessionCounters.resetPacketsRead();
            taskSessionCounters.resetPacketsSent();
        }

        this.comTaskExecutionComCommand = comTaskExecutionComCommand;
        this.currentTaskExecutionBuilder = Optional.of(this.sessionBuilder.addComTaskExecutionSession(comTaskExecution, comTaskExecution.getComTask(), now()));
        initializeJournalist();
        if (this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
            this.addProtocolDialectPropertiesAsJournalEntries(comTaskExecution);
        }
    }

    /**
     * Completes the execution of the {@link ComTaskExecution}.
     * <p>
     * <i>Need to keep the comTaskExecution to properly AOP the statistics</i>
     *
     * @param comTaskExecution The ComTaskExecution
     */
    public void completeExecutedComTask(ComTaskExecution comTaskExecution, ComTaskExecutionSession.SuccessIndicator successIndicator) {
        if (isConnected()) {
            this.getComPortRelatedComChannel().logRemainingBytes();
        }
        connectionLogger.completingTask(Thread.currentThread().getName(), comTaskExecution.getComTask().getName());
        try {
            comTaskExecutionCompleted(successIndicator);
        } finally {
            this.publish(
                    new ComTaskExecutionCompletionEvent(
                            new ComServerEventServiceProvider(),
                            comTaskExecution,
                            successIndicator,
                            this.getComPort(),
                            this.getConnectionTask()
                    ));
            if (executing != null) {
                executing.stop();
            }
            this.addStatisticalInformationForTaskSession();
        }
    }

    private void addStatisticalInformationForTaskSession() {
        if (this.isConnected() && this.currentTaskExecutionBuilder.isPresent()) {
            ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder = this.currentTaskExecutionBuilder.get();
            Counters taskSessionCounters = this.comPortRelatedComChannel.getTaskSessionCounters();
            comTaskExecutionSessionBuilder.addSentBytes(taskSessionCounters.getBytesSent());
            comTaskExecutionSessionBuilder.addReceivedBytes(taskSessionCounters.getBytesRead());
            comTaskExecutionSessionBuilder.addSentPackets(taskSessionCounters.getPacketsSent());
            comTaskExecutionSessionBuilder.addReceivedPackets(taskSessionCounters.getPacketsRead());
        }
    }

    private void comTaskExecutionCompleted(ComTaskExecutionSession.SuccessIndicator successIndicator) {
        if (this.currentTaskExecutionBuilder != null && currentTaskExecutionBuilder.isPresent()) {
            currentTaskExecutionBuilder.get().add(now(), successIndicator);
            this.currentTaskExecutionBuilder = Optional.empty();

            this.getStoreCommand().addAll(this.toDeviceCommands(this.comTaskExecutionComCommand));
        }
    }

    /**
     * Notify interested parties that the execution of the {@link ComTaskExecution} failed
     * because of an exceptional situation that was not necessarily anticipated.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    public void comTaskExecutionFailed(ComTaskExecution comTaskExecution) {
        connectionLogger.taskExecutionFailedDueToProblems(Thread.currentThread().getName(), comTaskExecution.getComTask().getName(), comTaskExecution.getDevice().getName());
        publish(new ComTaskExecutionFailureEvent(new ComServerEventServiceProvider(), comTaskExecution, getComPort(), getConnectionTask()));
        failWithProblems();
    }

    private List<DeviceCommand> toDeviceCommands(ComTaskExecutionComCommand comTaskExecutionComCommand) {
        List<CollectedData> collectedData = comTaskExecutionComCommand.getCollectedData();
        List<ServerCollectedData> serverCollectedData = collectedData.stream().map(ServerCollectedData.class::cast).collect(Collectors.toList());
        return getDeviceCommandFactory().newForAll(serverCollectedData, getDeviceCommandServiceProvider());
    }

    private DeviceCommandFactory getDeviceCommandFactory() {
        if (this.deviceCommandFactory == null) {
            deviceCommandFactory = new DeviceCommandFactoryImpl(this.comTaskExecution);
        }
        return deviceCommandFactory;
    }

    public void setDeviceCommandFactory(DeviceCommandFactory deviceCommandFactory) {
        this.deviceCommandFactory = deviceCommandFactory;
    }

    private void failWithProblems() {
        if (this.currentTaskExecutionBuilder != null && currentTaskExecutionBuilder.isPresent()) {
            currentTaskExecutionBuilder.get().add(now(), ComTaskExecutionSession.SuccessIndicator.Failure);
        }
    }

    protected void completeFailure(ComSession.SuccessIndicator successIndicator) {
        sessionBuilder.setFailedTasks(this.jobExecution.getFailedComTaskExecutions().size());
        sessionBuilder.setSuccessFulTasks(this.jobExecution.getSuccessfulComTaskExecutions().size());
        sessionBuilder.setNotExecutedTasks(this.jobExecution.getNotExecutedComTaskExecutions().size());

        this.createComSessionCommand(sessionBuilder, successIndicator);
    }

    protected void completeSuccessful() {
        sessionBuilder.setFailedTasks(this.jobExecution.getFailedComTaskExecutions().size());
        sessionBuilder.setSuccessFulTasks(this.jobExecution.getSuccessfulComTaskExecutions().size());
        sessionBuilder.setNotExecutedTasks(this.jobExecution.getNotExecutedComTaskExecutions().size());

        ComSession.SuccessIndicator successIndicator = Thread.currentThread().isInterrupted() ? ComSession.SuccessIndicator.Broken : ComSession.SuccessIndicator.Success;
        this.createComSessionCommand(sessionBuilder, successIndicator);
    }

    protected void completeOutsideComWindow() {
        ComSession.SuccessIndicator successIndicator = ComSession.SuccessIndicator.Success;
        this.createComSessionCommand(sessionBuilder, successIndicator);
    }

    private void executionStopWatchStart() {
        if (this.executing == null) {
            this.executing = new StopWatch();
        }
        this.executing.start();
    }

    private List<ConnectionTaskProperty> getConnectionTaskProperties() {
        return this.getComServerDAO().findProperties(this.connectionTask);
    }

    public boolean connectionFailed() {
        return this.connectionFailed;
    }

    private void createComSessionCommand(ComSessionBuilder comSessionBuilder, ComSession.SuccessIndicator successIndicator) {
        if (this.connectionTask instanceof OutboundConnectionTask) {
            this.getStoreCommand().
                    add(new CreateOutboundComSession(
                            now(),
                            this.comPort.getComServer().getCommunicationLogLevel(),
                            (ScheduledConnectionTask) this.connectionTask,
                            comSessionBuilder, successIndicator, serviceProvider.clock()));
        } else {
            this.getStoreCommand().
                    add(new CreateInboundComSession(
                            now(),
                            (InboundComPort) getComPort(),
                            (InboundConnectionTask) this.connectionTask,
                            comSessionBuilder, successIndicator, serviceProvider.clock()));
        }
    }

    public CompositeDeviceCommand getStoreCommand() {
        if (this.storeCommand == null) {
            this.storeCommand = new ComSessionRootDeviceCommand(this.getComPort().getComServer().getCommunicationLogLevel());
        }
        return this.storeCommand;
    }

    private Instant now() {
        return serviceProvider.clock().instant();
    }

    public ComCommandJournalist getJournalist() {
        return journalist;
    }

    public boolean hasBasicCheckFailed() {
        return comTaskExecutionComCommand.getGroupedDeviceCommand().hasBasicCheckFailedForThisGroupedDeviceCommand();
    }

    public void initializeJournalist() {
        this.journalist = new ComCommandJournalist(this, serviceProvider.clock());
    }

    public DeviceCommandServiceProvider getDeviceCommandServiceProvider() {
        return new DeviceCommandServiceProvider();
    }

    private void publish(ComServerEvent event) {
        this.eventPublisher().publish(event);
    }

    public interface ServiceProvider {

        Clock clock();

        NlsService nlsService();

        EventService eventService();

        IssueService issueService();

        ConnectionTaskService connectionTaskService();

        DeviceService deviceService();

        EventPublisher eventPublisher();

        MdcReadingTypeUtilService mdcReadingTypeUtilService();

        EngineService engineService();

        DeviceMessageService deviceMessageService();

    }

    private class ConnectionTaskPropertyCache implements ConnectionTaskPropertyProvider {
        private List<ConnectionTaskProperty> properties;

        @Override
        public List<ConnectionTaskProperty> getProperties() {
            if (this.properties == null) {
                this.setProperties(getConnectionTaskProperties());
            }
            return this.properties;
        }

        public void setProperties(List<ConnectionTaskProperty> properties) {
            this.properties = properties;
        }

        @Override
        public void saveAllProperties() {
            getConnectionTask().saveAllProperties();
        }

        @Override
        public void removeAllProperties() {
            getConnectionTask().removeAllProperties();
        }

        @Override
        public TypedProperties getTypedProperties() {
            return TypedProperties.empty();
        }
    }

    private class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return serviceProvider.deviceMessageService();
        }
    }

    private class DeviceCommandServiceProvider implements DeviceCommand.ServiceProvider {
        @Override
        public IssueService issueService() {
            return serviceProvider.issueService();
        }

        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public EngineService engineService() {
            return serviceProvider.engineService();
        }

        @Override
        public NlsService nlsService() {
            return serviceProvider.nlsService();
        }

        @Override
        public EventService eventService() {
            return serviceProvider.eventService();
        }

        public EventPublisher eventPublisher() {
            return serviceProvider.eventPublisher();
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return serviceProvider.deviceMessageService();
        }

    }
}