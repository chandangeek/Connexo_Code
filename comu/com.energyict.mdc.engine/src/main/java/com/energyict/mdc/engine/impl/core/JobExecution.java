package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.GenericDeviceProtocol;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.OfflineDeviceForComTaskGroup;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.CreateOutboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandFactoryImpl;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.aspects.journaling.ComCommandJournalist;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComChannelLogger;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComCommandLogger;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ProtocolTask;

import com.elster.jupiter.transaction.Transaction;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.tasks.history.CompletionCode;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.energyict.mdc.tasks.history.ComSession.SuccessIndicator.Broken;
import static com.energyict.mdc.tasks.history.ComTaskExecutionSession.SuccessIndicator.Failure;
import static com.energyict.mdc.tasks.history.ComTaskExecutionSession.SuccessIndicator.Success;

/**
 * Provides code reuse for in- and outbound {@link com.energyict.mdc.engine.model.ComPort ComPorts }
 * which perform one or more ComTasks.
 * It will be useful to group the AOP logging as well.
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/10/12
 * Time: 16:27
 */
public abstract class JobExecution implements ScheduledJob {
    private final ComPort comPort;
    private final ComServerDAO comServerDAO;
    private final DeviceCommandExecutor deviceCommandExecutor;

    private final ServiceProvider serviceProvider;
    private DeviceCommandExecutionToken token;
    private ExecutionContext executionContext;
    private PreparationContext preparationContext;

    public JobExecution(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
        this.preparationContext = new PreparationContext();
    }

    public void closeConnection() {
        if (getExecutionContext() != null) {
            getExecutionContext().close();
        }
    }

    public ComPort getComPort() {
        return comPort;
    }

    /**
     * Provide a list of {@link ComTaskExecution comTaskExecutions} which will be executed during this session
     *
     * @return the list of ComTaskExecutions
     */
    public abstract List<ComTaskExecution> getComTaskExecutions();

    public abstract ConnectionTask<?, ?> getConnectionTask();

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public abstract List<ComTaskExecution> getFailedComTaskExecutions();

    public abstract List<ComTaskExecution> getNotExecutedComTaskExecutions();

    public abstract List<ComTaskExecution> getSuccessfulComTaskExecutions();

    public String getThreadName() {
        return Thread.currentThread().getName();
    }

    public DeviceCommandExecutionToken getToken() {
        return token;
    }

    public void releaseToken() {
        this.getDeviceCommandExecutor().free(this.getToken());
    }

    @Override
    public void reschedule() {
        this.doReschedule(RescheduleBehavior.RescheduleReason.COMTASKS);
        this.completeSuccessfulComSession();
    }

    @Override
    public void reschedule(Throwable t, RescheduleBehavior.RescheduleReason rescheduleReason) {
        this.doReschedule(rescheduleReason);
        this.completeFailedComSession(t, this.getFailureIndicatorFor(rescheduleReason));
    }

    @Override
    public void setToken(DeviceCommandExecutionToken deviceCommandExecutionToken) {
        this.token = deviceCommandExecutionToken;
    }

    protected void completeFailedComSession(Throwable t, ComSession.SuccessIndicator reason) {
        this.getExecutionContext().fail(t, reason);
        this.getDeviceCommandExecutor().execute(this.getExecutionContext().getStoreCommand(), getToken());
    }

    protected void completeSuccessfulComSession() {
        this.getExecutionContext().complete();
        this.getDeviceCommandExecutor().execute(this.getExecutionContext().getStoreCommand(), getToken());
    }

    protected final void doReschedule(final RescheduleBehavior.RescheduleReason rescheduleReason) {
        final RescheduleBehavior retryBehavior = this.getRescheduleBehavior();
        serviceProvider.transactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                retryBehavior.performRescheduling(rescheduleReason);
            }
        });
    }

    /**
     * Performs the provided {@link PreparedComTaskExecution} and updates the
     * success-, failed-, notExecutedComTasks.
     *
     * @param preparedComTaskExecution the comTask to execute
     */
    protected void executeAndUpdateSuccessRate(PreparedComTaskExecution preparedComTaskExecution) {
        boolean success = false;
        try {
            success = execute(preparedComTaskExecution);
        } catch (RuntimeException e) {
            success = false;
            throw e;
        } finally {
            if (!getExecutionContext().hasBasicCheckFailed()) {
                getNotExecutedComTaskExecutions().remove(preparedComTaskExecution.getComTaskExecution());
                if (success) {
                    getSuccessfulComTaskExecutions().add(preparedComTaskExecution.getComTaskExecution());
                    getExecutionContext().getComSessionBuilder().incrementSuccessFulTasks();
                } else {
                    getFailedComTaskExecutions().add(preparedComTaskExecution.getComTaskExecution());
                    getExecutionContext().getComSessionBuilder().incrementFailedTasks();
                }
            }
            // if the basicCheck fails, then all other ComTasks will not be executed (except the logOff etc.)
        }
    }

    /**
     * Find or create the {@link ComChannel}
     * that will be used during the execution
     * of the <i>tasks</i>. If the ComChannel exists, then it is
     * most likely that you do not need to recreate it again
     * (otherwise it might be that an undesired connect/disconnect occurs)
     *
     * @return the ComChannel that will be used during the execution of the tasks.
     * @throws ConnectionException
     */
    protected abstract ComPortRelatedComChannel findOrCreateComChannel() throws ConnectionException;

    protected DeviceCommandExecutor getDeviceCommandExecutor() {
        return deviceCommandExecutor;
    }

    /**
     * Indicate whether a connection has been set-up
     *
     * @return true if the connection is up, false otherwise
     */
    protected abstract boolean isConnected();

    protected ExecutionContext newExecutionContext(ConnectionTask<?, ?> connectionTask, ComPort comPort) {
        return newExecutionContext(connectionTask, comPort, true);
    }

    protected ExecutionContext newExecutionContext(ConnectionTask<?, ?> connectionTask, ComPort comPort, boolean logConnectionProperties) {
        return new ExecutionContext(this, connectionTask, comPort, logConnectionProperties, new ComCommandServiceProvider());
    }

    protected void performPreparedComTaskExecution(PreparedComTaskExecution preparedComTaskExecution) {
        try {
            /* No need to attempt to lock the ScheduledComTask
             * as we are locking on the ConnectionTask. */
            this.executeAndUpdateSuccessRate(preparedComTaskExecution);
        } catch (ComServerRuntimeException e) {
            if (checkIfWeCanPerformOtherComTaskExecutionsBasedOnConnectionRelatedExceptions(e)) {
                throw e;
            } else if (checkWhetherAllNextComTasksShouldNotBeExecutedBasedonBasicCheckFailure(e)) {
                getExecutionContext().setBasicCheckFailed(true);
            }
            // else the task will be logged, next task can be executed
        }
    }

    protected List<PreparedComTaskExecution> prepareAll(final List<? extends ComTaskExecution> comTaskExecutions) {
        return this.serviceProvider.transactionService().execute(new PrepareAllTransaction(comTaskExecutions));
    }

    protected PreparedComTaskExecution prepareOne(final ComTaskExecution comTaskExecution) {
        return this.serviceProvider.transactionService().execute(new PrepareTransaction(comTaskExecution));
    }

    protected void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    boolean execute(PreparedComTaskExecution preparedComTaskExecution) {
        ComTaskExecutionSession.SuccessIndicator successIndicator = Success;
        try {
            this.start(preparedComTaskExecution.getComTaskExecution());

            CommandRoot commandRoot = preparedComTaskExecution.getCommandRoot();
            this.executionContext.setCommandRoot(commandRoot);
            commandRoot.executeFor(preparedComTaskExecution, this.executionContext);
            boolean hasProblems = executionContext.getCommandRoot().getProblems().isEmpty();
            successIndicator = hasProblems ? Success : Failure;
            return hasProblems;
        } catch (Throwable t) {
            this.failure(preparedComTaskExecution.getComTaskExecution(), t);
            successIndicator = Failure;
            throw t;
        } finally {
            this.completeExecutedComTask(preparedComTaskExecution.getComTaskExecution(), successIndicator);
        }
    }

    final ComServerDAO getComServerDAO() {
        return comServerDAO;
    }

    ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    private boolean checkIfWeCanPerformOtherComTaskExecutionsBasedOnConnectionRelatedExceptions(ComServerRuntimeException e) {
        return CommunicationException.class.isAssignableFrom(e.getClass());
    }

    private boolean checkWhetherAllNextComTasksShouldNotBeExecutedBasedonBasicCheckFailure(ComServerRuntimeException e) {
        if (DeviceConfigurationException.class.isAssignableFrom(e.getClass())) {
            switch (e.getMessageId()) {
                case "CSC-CONF-112":
                case "CSC-CONF-134":
                    return true;
            }
        }
        return false;
    }

    /**
     * Completes the execution of the {@link ComTaskExecution}.
     * <p/>
     * <i>Need to keep the comTaskExecution to properly AOP the statistics</i>
     *
     * @param comTaskExecution The ComTaskExecution
     * @param successIndicator
     */
    private void completeExecutedComTask(ComTaskExecution comTaskExecution, ComTaskExecutionSession.SuccessIndicator successIndicator) {
        this.getExecutionContext().comTaskExecutionCompleted(successIndicator);
    }

    private void connected(ComPortRelatedComChannel comChannel) {
        this.preparationContext.setComChannel(comChannel);
    }

    /**
     * Notify interested parties that the execution of the {@link ComTaskExecution} failed
     * because of an exceptional situation that was not necessarily anticipated.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param t                The failure
     */
    private void failure(ComTaskExecution comTaskExecution, Throwable t) {
        this.getExecutionContext().fail(comTaskExecution, t);
    }

    private CommandRoot.ServiceProvider getComCommandServiceProvider() {
        return new ComCommandServiceProvider();
    }

    private ComSession.SuccessIndicator getFailureIndicatorFor(RescheduleBehavior.RescheduleReason rescheduleReason) {
        if (RescheduleBehavior.RescheduleReason.CONNECTION_BROKEN.equals(rescheduleReason)) {
            return Broken;
        }
        return ComSession.SuccessIndicator.SetupError;
    }

    private PreparedComTaskExecution getPreparedComTaskExecution(ComTaskPreparationContext comTaskPreparationContext, ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, BaseDevice<?, ?, ?> masterDevice, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        final List<? extends ProtocolTask> protocolTasks = comTaskExecution.getComTask().getProtocolTasks();
        Collections.sort(protocolTasks, BasicCheckTasks.FIRST);
        comTaskPreparationContext.getCommandCreator().
                createCommands(
                        comTaskPreparationContext.getRoot(),
                        this.getProtocolDialectTypedProperties(comTaskExecution, serviceProvider.deviceDataService()),
                        this.preparationContext.getComChannelPlaceHolder(),
                        comTaskPreparationContext.getOfflineDevice(),
                        protocolTasks,
                        deviceProtocolSecurityPropertySet,
                        connectionSteps, comTaskExecution, this.serviceProvider.issueService());
        return new PreparedComTaskExecution(comTaskExecution, comTaskPreparationContext.getRoot(), comTaskPreparationContext.getDeviceProtocol());
    }

    private static TypedProperties getProtocolDialectTypedProperties(ComTaskExecution comTaskExecution, DeviceDataService deviceDataService) {
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = comTaskExecution.getProtocolDialectConfigurationProperties();
        Device device = deviceDataService.findDeviceById(comTaskExecution.getDevice().getId());
        ProtocolDialectProperties protocolDialectPropertiesWithName = device.getProtocolDialectProperties(protocolDialectConfigurationProperties.getDeviceProtocolDialectName());
        if (protocolDialectPropertiesWithName == null) {
            return TypedProperties.inheritingFrom(protocolDialectConfigurationProperties.getTypedProperties());
        } else {
            return protocolDialectPropertiesWithName.getTypedProperties();
        }
    }

    private RescheduleBehavior getRescheduleBehavior() {
        if (!InboundConnectionTask.class.isAssignableFrom(getConnectionTask().getClass())) {
            ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) getConnectionTask();
            if (scheduledConnectionTask.getConnectionStrategy().equals(ConnectionStrategy.MINIMIZE_CONNECTIONS)) {
                return new RescheduleBehaviorForMinimizeConnections(getComServerDAO(),
                        this.getSuccessfulComTaskExecutions(),
                        this.getFailedComTaskExecutions(),
                        this.getNotExecutedComTaskExecutions(),
                        getConnectionTask());
            } else {
                return new RescheduleBehaviorForAsap(getComServerDAO(),
                        this.getSuccessfulComTaskExecutions(),
                        this.getFailedComTaskExecutions(),
                        this.getNotExecutedComTaskExecutions(),
                        getConnectionTask(),
                        getExecutionContext());
            }
        } else {
            return new RescheduleBehaviorForInbound(getComServerDAO(),
                    this.getSuccessfulComTaskExecutions(),
                    this.getFailedComTaskExecutions(),
                    this.getNotExecutedComTaskExecutions(),
                    getConnectionTask());
        }
    }

    /**
     * Starts the execution of the {@link ComTaskExecution}.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    private void start(ComTaskExecution comTaskExecution) {
        this.getComServerDAO().executionStarted(comTaskExecution, this.getComPort());
        getExecutionContext().start(comTaskExecution);
    }

    enum BasicCheckTasks implements Comparator<ProtocolTask> {
        FIRST;

        @Override
        public int compare(ProtocolTask o1, ProtocolTask o2) {
            if (o1 instanceof BasicCheckTask) {
                return o2 instanceof BasicCheckTask ? 0 : -1;
            }
            return o2 instanceof BasicCheckTask ? 1 : 0;
        }
    }

    public static final class ExecutionContext {
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
            sessionBuilder = this.serviceProvider.getTaskHistoryService().buildComSession(this.connectionTask, this.connectionTask.getComPortPool(), this.comPort, now());
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
            this.journalist = new ComCommandJournalist(this.getCurrentTaskExecutionBuilder(), serviceProvider.getClock());
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
            TypedProperties protocolDialectTypedProperties = getProtocolDialectTypedProperties(comTaskExecution, serviceProvider.getDeviceDataService());
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
                                comSessionBuilder, successIndicator));
            } else {
                this.getStoreCommand().
                        add(new CreateInboundComSession(
                                (InboundComPort) getComPort(),
                                (InboundConnectionTask) this.connectionTask,
                                comSessionBuilder, successIndicator));
            }
        }

        private void createComSessionJournalEntry(String message) {
            getComSessionBuilder().addJournalEntry(now(), message, null);
        }

        private void createComTaskExecutionMessageJournalEntry(String message) {
            getCurrentTaskExecutionBuilder().addComTaskExecutionMessageJournalEntry(now(), "", message);
        }

        private void fail(ComTaskExecution comTaskExecution, Throwable t) {
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
            return serviceProvider.getClock().now();
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
            return new DeviceCommandFactoryImpl().newForAll(serverCollectedData, this.serviceProvider.getIssueService());
        }
    }

    /**
     * Provides context information to the process
     * that prepares {@link ComTaskExecution}s for execution.
     */
    protected static class PreparationContext {
        private ComChannelPlaceHolder comChannelPlaceHolder = ComChannelPlaceHolder.empty();

        public ComChannelPlaceHolder getComChannelPlaceHolder() {
            return comChannelPlaceHolder;
        }

        public void setComChannel(ComPortRelatedComChannel comChannel) {
            this.comChannelPlaceHolder.setComChannel(comChannel);
        }
    }

    public static class PreparedComTaskExecution {
        private final ComTaskExecution comTaskExecution;
        private final CommandRoot commandRoot;
        private final DeviceProtocol deviceProtocol;

        public PreparedComTaskExecution(ComTaskExecution comTaskExecution, CommandRoot commandRoot, DeviceProtocol deviceProtocol) {
            super();
            this.comTaskExecution = comTaskExecution;
            this.commandRoot = commandRoot;
            this.deviceProtocol = deviceProtocol;
        }

        public ComTaskExecution getComTaskExecution() {
            return comTaskExecution;
        }

        public CommandRoot getCommandRoot() {
            return commandRoot;
        }

        public DeviceProtocol getDeviceProtocol() {
            return deviceProtocol;
        }
    }

    private class ComCommandServiceProvider implements CommandRoot.ServiceProvider {
        @Override
        public Clock getClock() {
            return JobExecution.this.serviceProvider.clock();
        }

        @Override
        public DeviceDataService getDeviceDataService() {
            return JobExecution.this.serviceProvider.deviceDataService();
        }

        @Override
        public IssueService getIssueService() {
            return JobExecution.this.serviceProvider.issueService();
        }

        @Override
        public MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
            return JobExecution.this.serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public TaskHistoryService getTaskHistoryService() {
            return JobExecution.this.serviceProvider.taskHistoryService();
        }
    }

    /**
     * Provides relevant information used for preparing a ComTaskExecution.
     * This information is reusable for different ComTaskExecutions of the same device.
     */
    public class ComTaskPreparationContext {
        private DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution;
        private OfflineDevice offlineDevice;
        private CommandRoot root;
        private DeviceProtocol deviceProtocol;
        private CommandCreator commandCreator;

        public ComTaskPreparationContext(DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution) {
            this.deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecution;
        }

        public CommandCreator getCommandCreator() {
            return commandCreator;
        }

        public DeviceProtocol getDeviceProtocol() {
            return deviceProtocol;
        }

        public OfflineDevice getOfflineDevice() {
            return offlineDevice;
        }

        public CommandRoot getRoot() {
            return root;
        }

        public ComTaskPreparationContext invoke() {
            this.takeDeviceOffline();
            root = new CommandRootImpl(offlineDevice, getExecutionContext(), getComCommandServiceProvider());
            DeviceProtocolPluggableClass protocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
            deviceProtocol = protocolPluggableClass.getDeviceProtocol();
            commandCreator = CommandFactory.commandCreatorForPluggableClass(protocolPluggableClass);
            return this;
        }

        public void setRoot(CommandRoot root) {
            this.root = root;
        }

        private void takeDeviceOffline() {
            final OfflineDeviceForComTaskGroup offlineDeviceForComTaskGroup = new OfflineDeviceForComTaskGroup(deviceOrganizedComTaskExecution.getComTaskExecutions());
            offlineDevice = new OfflineDeviceImpl((Device) deviceOrganizedComTaskExecution.getDevice(), offlineDeviceForComTaskGroup);
        }
    }

    private class PrepareAllTransaction implements Transaction<List<PreparedComTaskExecution>> {
        private final List<? extends ComTaskExecution> comTaskExecutions;

        @Override
        public List<PreparedComTaskExecution> perform() {
            List<PreparedComTaskExecution> result = new ArrayList<>();
            ComTaskExecutionOrganizer organizer = new ComTaskExecutionOrganizer(serviceProvider.deviceConfigurationService());
            for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : organizer.defineComTaskExecutionOrders(comTaskExecutions)) {
                ComTaskPreparationContext comTaskPreparationContext = new ComTaskPreparationContext(deviceOrganizedComTaskExecution).invoke();
                for (DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps comTaskWithSecurityAndConnectionSteps : deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity()) {
                    ComTaskExecution comTaskExecution = comTaskWithSecurityAndConnectionSteps.getComTaskExecution();
                    ComTaskExecutionConnectionSteps connectionSteps = comTaskWithSecurityAndConnectionSteps.getComTaskExecutionConnectionSteps();
                    DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet();
                    PreparedComTaskExecution preparedComTaskExecution =
                            getPreparedComTaskExecution(
                                    comTaskPreparationContext,
                                    comTaskExecution,
                                    connectionSteps,
                                    deviceOrganizedComTaskExecution.getDevice(),
                                    deviceProtocolSecurityPropertySet);
                    result.add(preparedComTaskExecution);
                }
                //GenericDeviceProtocols can reorganize the commands
                if (GenericDeviceProtocol.class.isAssignableFrom(comTaskPreparationContext.getDeviceProtocol().getClass())) {
                    comTaskPreparationContext.setRoot(((GenericDeviceProtocol) comTaskPreparationContext.getDeviceProtocol()).organizeComCommands(comTaskPreparationContext.getRoot()));
                }
            }
            return result;
        }

        private PrepareAllTransaction(List<? extends ComTaskExecution> comTaskExecutions) {
            this.comTaskExecutions = comTaskExecutions;
        }
    }

    private class PrepareTransaction implements Transaction<PreparedComTaskExecution> {
        private final ComTaskExecution comTaskExecution;

        @Override
        public PreparedComTaskExecution perform() {
            ComTaskExecutionOrganizer organizer = new ComTaskExecutionOrganizer(serviceProvider.deviceConfigurationService());
            final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = organizer.defineComTaskExecutionOrders(Arrays.asList(comTaskExecution));
            final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
            if (deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().size() == 1) {
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps comTaskWithSecurityAndConnectionSteps = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(0);
                ComTaskPreparationContext comTaskPreparationContext = new ComTaskPreparationContext(deviceOrganizedComTaskExecution).invoke();
                return getPreparedComTaskExecution(
                        comTaskPreparationContext,
                        comTaskWithSecurityAndConnectionSteps.getComTaskExecution(),
                        comTaskWithSecurityAndConnectionSteps.getComTaskExecutionConnectionSteps(),
                        deviceOrganizedComTaskExecution.getDevice(),
                        comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet());
            } else {
                throw CodingException.incorrectNumberOfPreparedComTaskExecutions(1, deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().size());
            }
        }

        private PrepareTransaction(ComTaskExecution comTaskExecution) {
            super();
            this.comTaskExecution = comTaskExecution;
        }
    }
}