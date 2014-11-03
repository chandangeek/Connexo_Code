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
import com.energyict.mdc.engine.impl.core.aspects.journaling.ComCommandJournalist;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComCommandLogger;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

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

        public DeviceService deviceDataService();

    }

    private final JobExecution jobExecution;
    private final ServiceProvider serviceProvider;

    private boolean connectionFailed;
    private ComPortRelatedComChannel comPortRelatedComChannel;
    private ComSessionBuilder sessionBuilder;
    private ComTaskExecutionSessionBuilder currentTaskExecutionBuilder;
    private ComCommandJournalist journalist;
    private ComCommandLogger comCommandLogger;
    private ComPort comPort;
    private ConnectionTask<?, ?> connectionTask;
    private ConnectionTaskPropertyCache connectionTaskPropertyCache = new ConnectionTaskPropertyCache();
    private ComTaskExecution comTaskExecution;
    private Logger logger;

    private CommandRoot commandRoot;
    private CompositeDeviceCommand storeCommand;
    private boolean basicCheckFailed = false;
    private StopWatch connecting;
    private StopWatch executing;

    public ExecutionContext(JobExecution jobExecution, ConnectionTask<?, ?> connectionTask, ComPort comPort, ServiceProvider serviceProvider) {
        this(jobExecution, connectionTask, comPort, true, serviceProvider);
    }

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
        this.logger = Logger.getAnonymousLogger();  // Start with an anonymous one, refine it later
    }

    public void close() {
        // Check if establishing the connection succeeded
        if (this.isConnected()) {
            this.comPortRelatedComChannel.close();
            this.addStatisticalInformationToComSession();
        }
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

    public void comTaskExecutionCompleted(ComTaskExecution comTaskExecution, ComTaskExecutionSession.SuccessIndicator successIndicator) {
        this.executing.stop();
        this.addStatisticalInformationForTaskSession();
        this.comTaskExecutionCompleted(successIndicator);
    }

    public void comTaskExecutionCompleted(ComTaskExecutionSession.SuccessIndicator successIndicator) {
        if (this.isConnected()) {
            this.comPortRelatedComChannel.logRemainingBytes();
        }
        if (hasBasicCheckFailed()) {
            String commandDescription = "ComTask will be rescheduled due to the failure of the BasicCheck task.";
            this.currentTaskExecutionBuilder.addComCommandJournalEntry(now(), CompletionCode.Rescheduled, "", commandDescription);
        }
        currentTaskExecutionBuilder.add(now(), successIndicator);
        this.currentTaskExecutionBuilder = null;
        this.getStoreCommand().addAll(this.deviceCommandsForCurrentComTaskExecution());
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
        if (this.getCurrentTaskExecutionBuilder() != null) {
            this.createComTaskExecutionMessageJournalEntry(logLevel, message);
        } else {
            this.createComSessionJournalEntry(logLevel, message);
        }
    }

    public void fail(Throwable t, ComSession.SuccessIndicator reason) {
        sessionBuilder.addJournalEntry(now(), ComServer.LogLevel.ERROR, messageFor(t), t);
        this.createComSessionCommand(sessionBuilder, reason);
    }

    public void failForRetryAsapComTaskExec(ComTaskExecution notExecutedComTaskExecution, Throwable t) {
        fail(notExecutedComTaskExecution, t);
        comTaskExecutionCompleted(Success);
    }

    public ComPortRelatedComChannel getComPortRelatedComChannel() {
        return comPortRelatedComChannel;
    }

    public ComCommandLogger getComCommandLogger() {
        return comCommandLogger;
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

    public ComTaskExecutionSessionBuilder getCurrentTaskExecutionBuilder() {
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

    public CompositeDeviceCommand getStoreCommand() {
        if (this.storeCommand == null) {
            this.storeCommand = new ComSessionRootDeviceCommand(this.getComPort().getComServer().getCommunicationLogLevel());
        }
        return this.storeCommand;
    }

    public boolean hasBasicCheckFailed() {
        return basicCheckFailed;
    }

    public void initializeJournalist() {
        this.journalist = new ComCommandJournalist(this.getCurrentTaskExecutionBuilder(), serviceProvider.clock());
    }

    public void markComTaskExecutionForConnectionSetupError(String reason) {
        if (this.currentTaskExecutionBuilder != null) { // Check if the failure occurred in the context of an executing ComTask
            currentTaskExecutionBuilder.addComCommandJournalEntry(now(), CompletionCode.ConnectionError, reason, "ConnectionType - Connect");
        }
    }

    public void setBasicCheckFailed(boolean basicCheckFailed) {
        this.basicCheckFailed = basicCheckFailed;
    }

    public void setComCommandLogger(ComCommandLogger comCommandLogger) {
        this.comCommandLogger = comCommandLogger;
    }

    public void setComSessionShadow(ComSessionBuilder comSessionShadow) {
        this.sessionBuilder = comSessionShadow;
    }

    public void setCommandRoot(CommandRoot commandRoot) {
        this.commandRoot = commandRoot;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void prepareStart(ComTaskExecution comTaskExecution) {
        this.executing.start();
        if (this.isConnected()) {
            Counters taskSessionCounters = this.comPortRelatedComChannel.getTaskSessionCounters();
            taskSessionCounters.resetBytesRead();
            taskSessionCounters.resetBytesSent();
            taskSessionCounters.resetPacketsRead();
            taskSessionCounters.resetPacketsSent();
        }
    }

    public void start(ComTaskExecution comTaskExecution) {
        this.comTaskExecution = comTaskExecution;
        this.currentTaskExecutionBuilder = sessionBuilder.addComTaskExecutionSession(comTaskExecution, connectionTask.getDevice(), now());
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
            sessionBuilder.addJournalEntry(now(), ComServer.LogLevel.INFO, builder.toString(), null);
        }
    }

    private List<ConnectionTaskProperty> getConnectionTaskProperties() {
        return this.getComServerDAO().findProperties(this.connectionTask);
    }

    private void addProtocolDialectPropertiesAsJournalEntries(ComTaskExecution comTaskExecution) {
        TypedProperties protocolDialectTypedProperties = JobExecution.getProtocolDialectTypedProperties(comTaskExecution);
        if (!protocolDialectTypedProperties.propertyNames().isEmpty()) {
            currentTaskExecutionBuilder.addComTaskExecutionMessageJournalEntry(now(), ComServer.LogLevel.INFO, asString(protocolDialectTypedProperties), "");
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
                        this.serviceProvider.eventService(),
                        connectionTask,
                        this.comPort,
                        this.jobExecution.getComTaskExecutions()));
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
        getComSessionBuilder().addJournalEntry(now(), logLevel, message, null);
    }

    private void createComTaskExecutionMessageJournalEntry(ComServer.LogLevel logLevel, String message) {
        getCurrentTaskExecutionBuilder().addComTaskExecutionMessageJournalEntry(now(), logLevel, message, "");
    }

    void comTaskExecutionFailure(ComTaskExecution comTaskExecution, Throwable t) {
        this.fail(comTaskExecution, t);
        this.executing.stop();
        this.addStatisticalInformationForTaskSession();
    }

    void fail(ComTaskExecution comTaskExecution, Throwable t) {
        if (this.isConnected()) {
            this.comPortRelatedComChannel.logRemainingBytes();
        }
        if (this.currentTaskExecutionBuilder != null) { // Check if the failure occurred in the context of an executing ComTask
            String translated;
            if (t instanceof ComServerRuntimeException) {
                ComServerRuntimeException comServerRuntimeException = (ComServerRuntimeException) t;
                translated = comServerRuntimeException.translated(this.serviceProvider.nlsService());
            } else {
                translated = t.getLocalizedMessage();
            }
            currentTaskExecutionBuilder.addComCommandJournalEntry(now(), CompletionCode.UnexpectedError, translated, "General");
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

    private String messageFor(Throwable t) {
        if (t.getMessage() == null) {
            return t.toString();
        } else {
            return t.getMessage();
        }
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

    private List<DeviceCommand> toDeviceCommands(ComTaskExecutionComCommand commandRoot) {
        List<CollectedData> collectedData = commandRoot.getCollectedData();
        List<ServerCollectedData> serverCollectedData = new ArrayList<>(collectedData.size());
        for (CollectedData data : collectedData) {
            serverCollectedData.add((ServerCollectedData) data);
        }
        return new DeviceCommandFactoryImpl().newForAll(serverCollectedData, this.serviceProvider.issueService());
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

}
