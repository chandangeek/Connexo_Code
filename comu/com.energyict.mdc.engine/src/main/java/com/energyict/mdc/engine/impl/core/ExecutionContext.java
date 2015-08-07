package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import java.time.Clock;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.CreateOutboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandFactoryImpl;
import com.energyict.mdc.engine.impl.commands.store.PublishConnectionSetupFailureEvent;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommand;
import com.energyict.mdc.engine.impl.core.events.ComPortLogHandler;
import com.energyict.mdc.engine.impl.core.logging.ComPortConnectionLogger;
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
import com.energyict.mdc.engine.impl.core.logging.CompositeComPortConnectionLogger;
import com.energyict.mdc.engine.impl.meterdata.DeviceCommandFactory;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;
import com.energyict.mdc.tasks.ComTask;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.data.tasks.history.ComSession.SuccessIndicator.Broken;
import static com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession.SuccessIndicator.Success;

/**
 * Copyrights EnergyICT
 * Date: 19/05/2014
 * Time: 16:12
 */
public final class ExecutionContext implements JournalEntryFactory {

    private static final long NANOS_IN_MILLI = 1000000L;

    public interface ServiceProvider {

        public Clock clock();

        public NlsService nlsService();

        public EventService eventService();

        public IssueService issueService();

        public ConnectionTaskService connectionTaskService();

        public DeviceService deviceService();

        public EventPublisher eventPublisher();

        public MdcReadingTypeUtilService mdcReadingTypeUtilService();

        public EngineService engineService();

    }

    private final JobExecution jobExecution;
    private final ServiceProvider serviceProvider;

    private boolean connectionFailed;
    private ComPortRelatedComChannel comPortRelatedComChannel;
    private ComSessionBuilder sessionBuilder;
    private Optional<ComTaskExecutionSessionBuilder> currentTaskExecutionBuilder = Optional.empty();
    private ComCommandJournalist journalist;
    private ComPort comPort;
    private ConnectionTask<?, ?> connectionTask;
    private ConnectionTaskPropertyCache connectionTaskPropertyCache = new ConnectionTaskPropertyCache();
    private ComTaskExecution comTaskExecution;
    private CompositeComPortConnectionLogger connectionLogger;
    private CompositeLogger logger;

    private CommandRoot commandRoot;
    private CompositeDeviceCommand storeCommand;
    private boolean basicCheckFailed = false;
    private StopWatch connecting;
    private StopWatch executing;
    private DeviceCommandFactory deviceCommandFactory;

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
        Logger actualLogger = this.getAnonymousLogger(new ComPortLogHandler(this.getComPort(), this.serviceProvider.eventPublisher(), new ComServerEventServiceProvider()));
        ComPortConnectionLogger eventLogger = LoggerFactory.getLoggerFor(ComPortConnectionLogger.class, actualLogger);
        this.connectionLogger.add(eventLogger);
        this.logger.add(actualLogger);
    }

    private Logger getAnonymousLogger (Handler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(handler);
        return logger;
    }

    public void close() {
        // Check if establishing the connection succeeded
        if (this.isConnected()) {
            this.comPortRelatedComChannel.close();
            this.addStatisticalInformationToComSession();
        }
        /* closeConnection is called from finally block
         * even when the connection was never established.
         * So first test if there was a connection. */
        if (this.isConnected()) {
            this.publish(new CloseConnectionEvent(new ComServerEventServiceProvider(), this.getComPort(), this.getConnectionTask()));
        }
    }

    public Clock clock() {
        return this.serviceProvider.clock();
    }

    public EventPublisher eventPublisher() {
        return this.serviceProvider.eventPublisher();
    }

    private boolean isConnected() {
        return this.comPortRelatedComChannel != null;
    }

    private void addStatisticalInformationToComSession() {
        ComSessionBuilder comSessionBuilder = this.getComSessionBuilder();
        comSessionBuilder.connectDuration(Duration.ofMillis(this.connecting.getElapsed() / NANOS_IN_MILLI));
        comSessionBuilder.talkDuration(this.comPortRelatedComChannel.talkTime());
        Counters sessionCounters = this.comPortRelatedComChannel.getSessionCounters();
        comSessionBuilder.addSentBytes(sessionCounters.getBytesSent());
        comSessionBuilder.addReceivedBytes(sessionCounters.getBytesRead());
        comSessionBuilder.addSentPackets(sessionCounters.getPacketsSent());
        comSessionBuilder.addReceivedPackets(sessionCounters.getPacketsRead());
    }

    private void addStatisticalInformationForTaskSession() {
        if (this.isConnected() && this.getComSessionBuilder() != null) {
            ComSessionBuilder comSessionBuilder = this.getComSessionBuilder();
            Counters taskSessionCounters = this.comPortRelatedComChannel.getTaskSessionCounters();
            comSessionBuilder.addSentBytes(taskSessionCounters.getBytesSent());
            comSessionBuilder.addReceivedBytes(taskSessionCounters.getBytesRead());
            comSessionBuilder.addSentPackets(taskSessionCounters.getPacketsSent());
            comSessionBuilder.addReceivedPackets(taskSessionCounters.getPacketsRead());
        }
    }

    public void comTaskExecutionCompleted(JobExecution job,ComTaskExecution comTaskExecution, ComTaskExecutionSession.SuccessIndicator successIndicator) {
        this.connectionLogger.completingTask(job.getThreadName(), comTaskExecution.getComTasks().get(0).getName());
        this.executing.stop();
        this.addStatisticalInformationForTaskSession();
        this.comTaskExecutionCompleted(successIndicator);
        this.publish(
                new ComTaskExecutionCompletionEvent(
                        new ComServerEventServiceProvider(),
                        comTaskExecution,
                        successIndicator,
                        this.getComPort(),
                        this.getConnectionTask()
                ));
    }

    public void comTaskExecutionCompleted(ComTaskExecutionSession.SuccessIndicator successIndicator) {
        this.completeCurrentExecutionIfPresent(successIndicator);
        this.currentTaskExecutionBuilder = Optional.empty();
        this.getStoreCommand().addAll(this.deviceCommandsForCurrentComTaskExecution());
    }

    private void completeCurrentExecutionIfPresent(ComTaskExecutionSession.SuccessIndicator successIndicator) {
        if (this.isConnected()) {
            this.comPortRelatedComChannel.logRemainingBytes();
        }
        if (basickCheckHasFailed()) {
            String commandDescription = "ComTask will be rescheduled due to the failure of the BasicCheck task.";
            this.currentTaskExecutionBuilder.ifPresent(b -> b.addComCommandJournalEntry(now(), CompletionCode.Rescheduled, "", commandDescription));
        }
        this.currentTaskExecutionBuilder.ifPresent(b -> b.add(now(), successIndicator));
    }

    private List<DeviceCommand> deviceCommandsForCurrentComTaskExecution() {
        if (this.getCommandRoot() != null) {
            return this.toDeviceCommands(this.getCommandRoot().getComTaskRoot(this.comTaskExecution));
        } else {
            return Collections.emptyList();
        }
    }

    public void complete() {
        if (Thread.currentThread().isInterrupted()) {
            this.createComSessionCommand(sessionBuilder, Broken);
        } else {
            this.createComSessionCommand(sessionBuilder, ComSession.SuccessIndicator.Success);
        }
    }

    /**
     * Perform a logical connect if this has not been done yet.
     * Return true if the connect succeeded, false otherwise.
     *
     * @return true if the connect succeeded, false otherwise
     */
    public boolean connect() {
        try {
            this.connectionFailed = false;
            this.connecting = new StopWatch();
            this.executing = new StopWatch();
            this.executing.stop();  // Do not auto start but start it manually as soon as execution starts.
            this.setComPortRelatedComChannel(this.jobExecution.findOrCreateComChannel(this.connectionTaskPropertyCache));
            this.getComServerDAO().executionStarted(this.connectionTask, this.comPort.getComServer());
            return this.jobExecution.isConnected();
        } catch (ConnectionException e) {
            this.comPortRelatedComChannel = null;
            this.connectionFailed(e, this.connectionTask);
            throw new ConnectionSetupException(MessageSeeds.CONNECTION_FAILURE, e);
        } finally {
            this.connecting.stop();
        }
    }

    public void createJournalEntry(ComServer.LogLevel logLevel, String message) {
        if (this.currentTaskExecutionBuilder.isPresent()) {
            this.createComTaskExecutionMessageJournalEntry(logLevel, message);
        }
        else {
            this.createComSessionJournalEntry(logLevel, message);
        }
    }

    public void createComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription) {
        this.currentTaskExecutionBuilder.ifPresent(b -> b.addComCommandJournalEntry(timestamp, completionCode, errorDesciption, commandDescription));
    }

    public void fail(Throwable t, ComSession.SuccessIndicator reason) {
        sessionBuilder.addJournalEntry(now(), ComServer.LogLevel.ERROR, t);
        this.createComSessionCommand(sessionBuilder, reason);
    }

    public void failForRetryAsapComTaskExec(ComTaskExecution notExecutedComTaskExecution, Throwable t) {
        failWith(t);
        comTaskExecutionCompleted(Success);
    }

    public void reschedule(JobExecution job, List<ComTaskExecution> failedComTaskExecutions) {
        for (ComTaskExecution failedComTask : failedComTaskExecutions) {
            this.connectionLogger.reschedulingTask(job.getThreadName(), failedComTask.getComTasks().get(0).getName());
        }
    }

    public ComPortRelatedComChannel getComPortRelatedComChannel() {
        return comPortRelatedComChannel;
    }

    public ComPort getComPort() {
        return comPort;
    }

    public ComSessionBuilder getComSessionBuilder() {
        return sessionBuilder;
    }

    public ComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }

    public CommandRoot getCommandRoot() {
        return commandRoot;
    }

    public ConnectionTask<?, ?> getConnectionTask() {
        return connectionTask;
    }

    public Optional<ComTaskExecutionSessionBuilder> getCurrentTaskExecutionBuilder() {
        return currentTaskExecutionBuilder;
    }

    public JobExecution getJob() {
        return this.jobExecution;
    }

    public ComCommandJournalist getJournalist() {
        return journalist;
    }

    public Logger getLogger() {
        return logger;
    }

    public ComPortConnectionLogger getConnectionLogger() {
        return connectionLogger;
    }

    public CompositeDeviceCommand getStoreCommand() {
        if (this.storeCommand == null) {
            this.storeCommand = new ComSessionRootDeviceCommand(this.getComPort().getComServer().getCommunicationLogLevel());
        }
        return this.storeCommand;
    }

    public boolean basickCheckHasFailed() {
        return basicCheckFailed;
    }

    public void initializeJournalist() {
        this.journalist = new ComCommandJournalist(this, serviceProvider.clock());
    }

    public void markComTaskExecutionForConnectionSetupError(String reason) {
        this.currentTaskExecutionBuilder.ifPresent(b -> b.addComCommandJournalEntry(now(), CompletionCode.ConnectionError, reason, "ConnectionType - Connect"));
    }

    public void setBasicCheckFailed(boolean basicCheckFailed) {
        this.basicCheckFailed = basicCheckFailed;
    }

    public void setComSessionBuilder(ComSessionBuilder comSessionBuilder) {
        this.sessionBuilder = comSessionBuilder;
    }

    public void setCommandRoot(CommandRoot commandRoot) {
        this.commandRoot = commandRoot;
    }

    public void setLogger(Logger logger) {
        if (this.logger == null) {
            this.logger = new CompositeLogger();
        }
        this.logger.add(logger);
    }

    public void prepareStart(JobExecution job, ComTaskExecution comTaskExecution) {
        this.connectionLogger.startingTask(job.getThreadName(), comTaskExecution.getComTasks().get(0).getName());
        executionStopWatchStart();
        if (this.isConnected()) {
            Counters taskSessionCounters = this.comPortRelatedComChannel.getTaskSessionCounters();
            taskSessionCounters.resetBytesRead();
            taskSessionCounters.resetBytesSent();
            taskSessionCounters.resetPacketsRead();
            taskSessionCounters.resetPacketsSent();
        }
    }

    private void executionStopWatchStart() {
        if(this.executing == null){
            this.executing = new StopWatch();
        }
        this.executing.start();
    }

    public void executionStarted(ComTaskExecution comTaskExecution) {
        this.initializeJournalist();
        this.publish(
                new ComTaskExecutionStartedEvent(
                        new ComServerEventServiceProvider(),
                        comTaskExecution,
                        this.getComPort(),
                        this.getConnectionTask()
                ));
    }

    public void start(ComTaskExecution comTaskExecution, ComTask comTask) {
        this.completeCurrentExecutionIfPresent(ComTaskExecutionSession.SuccessIndicator.Success);   // Workaround for multiple ComTasks in ComSchedule
        this.comTaskExecution = comTaskExecution;
        this.currentTaskExecutionBuilder = Optional.of(this.sessionBuilder.addComTaskExecutionSession(comTaskExecution, comTask, connectionTask.getDevice(), now()));
        if (this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
            this.addProtocolDialectPropertiesAsJournalEntries(comTaskExecution);
        }
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

    private List<ConnectionTaskProperty> getConnectionTaskProperties() {
        return this.getComServerDAO().findProperties(this.connectionTask);
    }

    private void addProtocolDialectPropertiesAsJournalEntries(ComTaskExecution comTaskExecution) {
        TypedProperties protocolDialectTypedProperties = JobExecution.getProtocolDialectTypedProperties(comTaskExecution);
        if (!protocolDialectTypedProperties.propertyNames().isEmpty()) {
            currentTaskExecutionBuilder.ifPresent(b -> b.addComTaskExecutionMessageJournalEntry(now(), ComServer.LogLevel.INFO, asString(protocolDialectTypedProperties), ""));
        }
    }

    private void appendPropertyToMessage(StringBuilder builder, ConnectionTaskProperty property) {
        this.appendPropertyToMessage(builder, property.getName(), property.getValue());
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
        this.getStoreCommand().add(
                new PublishConnectionSetupFailureEvent(
                        connectionTask,
                        this.comPort,
                        this.jobExecution.getComTaskExecutions(),
                        getDeviceCommandServiceProvider()));
        this.publish(new CannotEstablishConnectionEvent(new ComServerEventServiceProvider(), this.getComPort(), connectionTask, e));
        this.connectionLogger.cannotEstablishConnection(e, this.getJob().getThreadName());
    }

    public boolean connectionFailed () {
        return this.connectionFailed;
    }

    private void createComSessionCommand(ComSessionBuilder comSessionBuilder, ComSession.SuccessIndicator successIndicator) {
        if (this.connectionTask instanceof OutboundConnectionTask) {
            this.getStoreCommand().
                    add(new CreateOutboundComSession(
                            this.comPort.getComServer().getCommunicationLogLevel(),
                            (ScheduledConnectionTask) this.connectionTask,
                            comSessionBuilder, successIndicator, serviceProvider.clock()));
        } else {
            this.getStoreCommand().
                    add(new CreateInboundComSession(
                            (InboundComPort) getComPort(),
                            (InboundConnectionTask) this.connectionTask,
                            comSessionBuilder, successIndicator, serviceProvider.clock()));
        }
    }

    private void createComSessionJournalEntry(ComServer.LogLevel logLevel, String message) {
        getComSessionBuilder().addJournalEntry(now(), logLevel, message);
    }

    private void createComTaskExecutionMessageJournalEntry(ComServer.LogLevel logLevel, String message) {
        this.currentTaskExecutionBuilder.ifPresent(b -> b.addComTaskExecutionMessageJournalEntry(now(), logLevel, message, ""));
    }

    void comTaskExecutionFailureDueToProblemsOfComTasks(JobExecution job, ComTaskExecution comTaskExecution) {
        this.publish(
                new ComTaskExecutionFailureEvent(
                        new ComServerEventServiceProvider(),
                        comTaskExecution,
                        this.getComPort(),
                        this.getConnectionTask()
                ));
        this.connectionLogger.taskExecutionFailedDueToProblems(job.getThreadName(), comTaskExecution.getComTasks().get(0).getName());
    }

    void comTaskExecutionFailure(JobExecution job, ComTaskExecution comTaskExecution, Throwable t) {
        this.failWith(t);
        this.executing.stop();
        this.addStatisticalInformationForTaskSession();
        this.publish(
                new ComTaskExecutionFailureEvent(
                        new ComServerEventServiceProvider(),
                        comTaskExecution,
                        this.getComPort(),
                        this.getConnectionTask(),
                        t
                ));
        this.connectionLogger.taskExecutionFailed(t, job.getThreadName(), comTaskExecution.getComTasks().get(0).getName());
    }

    void failWith(Throwable t) {
        if (this.isConnected()) {
            this.comPortRelatedComChannel.logRemainingBytes();
        }
        if (this.currentTaskExecutionBuilder.isPresent()) { // Check if the failure occurred in the context of an executing ComTask
            String translated;
            if (t instanceof ComServerRuntimeException) {
                ComServerRuntimeException comServerRuntimeException = (ComServerRuntimeException) t;
                translated = comServerRuntimeException.translated(this.serviceProvider.nlsService());
            } else {
                translated = t.getLocalizedMessage();
            }
            this.currentTaskExecutionBuilder.get().addComCommandJournalEntry(now(), CompletionCode.UnexpectedError, translated, "General");
        }
    }

    private ComServerDAO getComServerDAO() {
        return this.jobExecution.getComServerDAO();
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

    private Instant now() {
        return serviceProvider.clock().instant();
    }

    private void setComPortRelatedComChannel(ComPortRelatedComChannel comPortRelatedComChannel) {
        if (comPortRelatedComChannel != null) { // Is null for inbound communication via servlet technology
            comPortRelatedComChannel.setJournalEntryFactory(this);
            this.comPortRelatedComChannel = comPortRelatedComChannel;
            this.jobExecution.connected(comPortRelatedComChannel);
        }
    }

    private List<DeviceCommand> toDeviceCommands(ComTaskExecutionComCommand comTaskExecutionComCommand) {
        List<CollectedData> collectedData = comTaskExecutionComCommand.getCollectedData();
        List<ServerCollectedData> serverCollectedData = collectedData.stream().map(ServerCollectedData.class::cast).collect(Collectors.toList());
        return getDeviceCommandFactory().newForAll(serverCollectedData, getDeviceCommandServiceProvider());
    }

    private DeviceCommandFactory getDeviceCommandFactory() {
        if(this.deviceCommandFactory == null){
            deviceCommandFactory = new DeviceCommandFactoryImpl();
        }
        return deviceCommandFactory;
    }

    public void setDeviceCommandFactory(DeviceCommandFactory deviceCommandFactory) {
        this.deviceCommandFactory = deviceCommandFactory;
    }

    public DeviceCommandServiceProvider getDeviceCommandServiceProvider() {
        return new DeviceCommandServiceProvider();
    }

    private void publish (ComServerEvent event) {
        this.eventPublisher().publish(event);
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
        public TypedProperties getTypedProperties() {
            return TypedProperties.empty();
        }
    }

    private class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
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

    }

}
