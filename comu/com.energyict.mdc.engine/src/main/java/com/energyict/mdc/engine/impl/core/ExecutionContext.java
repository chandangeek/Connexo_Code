package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.CreateOutboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandFactoryImpl;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommand;
import com.energyict.mdc.engine.impl.core.aspects.journaling.ComCommandJournalist;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComChannelLogger;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComCommandLogger;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.tasks.history.CompletionCode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.energyict.mdc.tasks.history.ComSession.SuccessIndicator.Broken;
import static com.energyict.mdc.tasks.history.ComTaskExecutionSession.SuccessIndicator.Success;

/**
* Copyrights EnergyICT
* Date: 19/05/2014
* Time: 16:12
*/
public final class ExecutionContext {
    private final JobExecution jobExecution;
    private final CommandRoot.ServiceProvider serviceProvider;

    private ComPortRelatedComChannel comChannel;
    private ComSessionBuilder sessionBuilder;
    private ComTaskExecutionSessionBuilder currentTaskExecutionBuilder;
    private ComCommandJournalist journalist;
    private ComCommandLogger comCommandLogger;
    private ComChannelLogger comChannelLogger;
    private ComPort comPort;
    private ConnectionTask<?, ?> connectionTask;
    private ComTaskExecution comTaskExecution;
    private Logger logger;

    private CommandRoot commandRoot;
    private CompositeDeviceCommand storeCommand;
    private boolean basicCheckFailed = false;

    public ExecutionContext(JobExecution jobExecution, ConnectionTask<?, ?> connectionTask, ComPort comPort, CommandRoot.ServiceProvider serviceProvider) {
        this(jobExecution, connectionTask, comPort, true, serviceProvider);
    }

    public ExecutionContext(JobExecution jobExecution, ConnectionTask<?, ?> connectionTask, ComPort comPort, boolean logConnectionProperties, CommandRoot.ServiceProvider serviceProvider) {
        super();
        this.jobExecution = jobExecution;
        this.comPort = comPort;
        this.connectionTask = connectionTask;
        this.serviceProvider = serviceProvider;
        sessionBuilder = this.serviceProvider.taskHistoryService().buildComSession(this.connectionTask, this.connectionTask.getComPortPool(), this.comPort, now());
        if (logConnectionProperties && this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
            this.addConnectionPropertiesAsJournalEntries(this.connectionTask);
        }
        this.logger = Logger.getAnonymousLogger();  // Start with an anonymous one, refine it later
    }

    public void close() {
        // Check if establishing the connection succeeded
        if (this.comChannel != null) {
            this.comChannel.close();
        }
    }

    public void comTaskExecutionCompleted(ComTaskExecutionSession.SuccessIndicator successIndicator) {
        if (hasBasicCheckFailed()) {
            String commandDescription = "ComTask will be rescheduled due to the failure of the BasicCheck task.";
            this.currentTaskExecutionBuilder.addComCommandJournalEntry(now(), CompletionCode.Rescheduled, "", commandDescription);
        }
        currentTaskExecutionBuilder.add(now(), successIndicator);
        this.currentTaskExecutionBuilder = null;
        this.getStoreCommand().addAll(this.toDeviceCommands(this.getCommandRoot().getComTaskRoot(this.comTaskExecution)));
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
     * Return true if the connect succeeded, false otherwise
     *
     * @return true if the connect succeeded, false otherwise
     */
    public boolean connect() {
        try {
            this.setComChannel(this.jobExecution.findOrCreateComChannel());
            this.getComServerDAO().executionStarted(this.connectionTask, this.comPort.getComServer());
            return this.jobExecution.isConnected();
        } catch (ConnectionException e) {
            this.comChannel = null;
            this.connectionFailed(e, this.connectionTask);
            throw new ConnectionSetupException(e);
        }
    }

    public void createJournalEntry(String message) {
        if (this.getCurrentTaskExecutionBuilder() != null) {
            this.createComTaskExecutionMessageJournalEntry(message);
        } else {
            this.createComSessionJournalEntry(message);
        }
    }

    public void fail(Throwable t, ComSession.SuccessIndicator reason) {
        sessionBuilder.addJournalEntry(now(), messageFor(t), t);
        this.createComSessionCommand(sessionBuilder, reason);
    }

    public void failForRetryAsapComTaskExec(ComTaskExecution notExecutedComTaskExecution, Throwable t) {
        fail(notExecutedComTaskExecution, t);
        comTaskExecutionCompleted(Success);
    }

    public ComPortRelatedComChannel getComChannel() {
        return comChannel;
    }

    public ComChannelLogger getComChannelLogger() {
        return comChannelLogger;
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

    public void setComChannelLogger(ComChannelLogger comChannelLogger) {
        this.comChannelLogger = comChannelLogger;
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

    public void start(ComTaskExecution comTaskExecution) {
        this.comTaskExecution = comTaskExecution;
        this.currentTaskExecutionBuilder = sessionBuilder.addComTaskExecutionSession(comTaskExecution, connectionTask.getDevice(), now());
        if (this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
            this.addProtocolDialectPropertiesAsJournalEntries(comTaskExecution);
        }
    }

    private void addConnectionPropertiesAsJournalEntries(ConnectionTask<?, ?> connectionTask) {
        List<ConnectionTaskProperty> connectionProperties = connectionTask.getProperties();
        if (!connectionProperties.isEmpty()) {
            StringBuilder builder = new StringBuilder("Connection properties: ");
            Holder<String> separator = HolderBuilder.first("").andThen(", ");
            for (ConnectionTaskProperty connectionProperty : connectionProperties) {
                builder.append(separator.get());
                this.appendPropertyToMessage(builder, connectionProperty);
            }
            sessionBuilder.addJournalEntry(now(), builder.toString(), null);
        }
    }

    private void addProtocolDialectPropertiesAsJournalEntries(ComTaskExecution comTaskExecution) {
        TypedProperties protocolDialectTypedProperties = JobExecution.getProtocolDialectTypedProperties(comTaskExecution, serviceProvider.deviceDataService());
        if (!protocolDialectTypedProperties.propertyNames().isEmpty()) {
            currentTaskExecutionBuilder.addComTaskExecutionMessageJournalEntry(now(), "", asString(protocolDialectTypedProperties));
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
     * <p/>
     * Note: Arguments are used by AOP
     */
    private void connectionFailed(ConnectionException e, ConnectionTask<?, ?> connectionTask) {
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

    private void createComSessionJournalEntry(String message) {
        getComSessionBuilder().addJournalEntry(now(), message, null);
    }

    private void createComTaskExecutionMessageJournalEntry(String message) {
        getCurrentTaskExecutionBuilder().addComTaskExecutionMessageJournalEntry(now(), "", message);
    }

    void fail(ComTaskExecution comTaskExecution, Throwable t) {
        if (this.currentTaskExecutionBuilder != null) { // Check if the failure occurred in the context of an executing ComTask
            if (!ComServerRuntimeException.class.isAssignableFrom(t.getClass())) {
                currentTaskExecutionBuilder.addComCommandJournalEntry(now(), CompletionCode.UnexpectedError, t.getLocalizedMessage(), "General");
            }// else the task will be logged, next task can be executed
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
        return t.getMessage() == null ? t.toString() : t.getMessage();
    }

    private Date now() {
        return serviceProvider.clock().now();
    }

    private void setComChannel(ComPortRelatedComChannel comChannel) {
        this.comChannel = comChannel;
        this.jobExecution.connected(comChannel);
    }

    private List<DeviceCommand> toDeviceCommands(ComTaskExecutionComCommand commandRoot) {
        List<CollectedData> collectedData = commandRoot.getCollectedData();
        List<ServerCollectedData> serverCollectedData = new ArrayList<>(collectedData.size());
        for (CollectedData data : collectedData) {
            serverCollectedData.add((ServerCollectedData) data);
        }
        return new DeviceCommandFactoryImpl().newForAll(serverCollectedData, this.serviceProvider.issueService());
    }
}
