package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.BusinessException;
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
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.CreateOutboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
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
import com.energyict.mdc.issues.Problem;
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
import org.joda.time.Duration;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

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

    /**
     * Provide a list of {@link ComTaskExecution comTaskExecutions} which will be executed during this session
     *
     * @return the list of ComTaskExecutions
     */
    public abstract List<ComTaskExecution> getComTaskExecutions();

    /**
     * Indicate whether a connection has been set-up
     *
     * @return true if the connection is up, false otherwise
     */
    protected abstract boolean isConnected();

    public abstract List<ComTaskExecution> getNotExecutedComTaskExecutions();

    public abstract List<ComTaskExecution> getFailedComTaskExecutions();

    public abstract List<ComTaskExecution> getSuccessfulComTaskExecutions();

    public abstract ConnectionTask<?, ?> getConnectionTask();

    public JobExecution(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
        this.preparationContext = new PreparationContext();
    }

    public ComPort getComPort() {
        return comPort;
    }

    protected ComServerDAO getComServerDAO() {
        return comServerDAO;
    }

    protected PreparedComTaskExecution getPreparedComTaskExecution(ComTaskPreparationContext comTaskPreparationContext, ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, BaseDevice<?, ?, ?> masterDevice, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        final List<? extends ProtocolTask> protocolTasks = comTaskExecution.getComTask().getProtocolTasks();
        makeSureBasicCheckTaskIsInFrontOfTheList(protocolTasks);
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

    /**
     * Adapt the given list so the BasicCheckTask is the first element of the list.
     *
     * @param protocolTasks the given list of ProtocolTasks
     */
    private void makeSureBasicCheckTaskIsInFrontOfTheList(List<? extends ProtocolTask> protocolTasks) {
        int basicCheckIndex = -1;
        for (ProtocolTask protocolTask : protocolTasks) {
            if (BasicCheckTask.class.isAssignableFrom(protocolTask.getClass())) {
                basicCheckIndex = protocolTasks.indexOf(protocolTask) + 1;
            }
        }
        if (basicCheckIndex != -1) {
            Collections.rotate(protocolTasks.subList(0, basicCheckIndex), 1);
        }
    }

    protected List<PreparedComTaskExecution> prepareAll(final List<? extends ComTaskExecution> comTaskExecutions) {
        return this.serviceProvider.transactionService().execute(new PrepareAllTransaction(comTaskExecutions));
    }

    protected PreparedComTaskExecution prepareOne(final ComTaskExecution comTaskExecution) {
        return this.serviceProvider.transactionService().execute(new PrepareTransaction(comTaskExecution));
    }

    private void connected(ComPortRelatedComChannel comChannel) {
        this.preparationContext.setComChannel(comChannel);
    }

    protected DeviceCommandExecutor getDeviceCommandExecutor() {
        return deviceCommandExecutor;
    }

    public DeviceCommandExecutionToken getToken() {
        return token;
    }

    public void releaseToken() {
        this.getDeviceCommandExecutor().free(this.getToken());
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    protected void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    protected boolean execute(PreparedComTaskExecution preparedComTaskExecution) {
        ComTaskExecutionSession.SuccessIndicator successIndicator = ComTaskExecutionSession.SuccessIndicator.Success;
        try {
            this.start(preparedComTaskExecution.getComTaskExecution());

            CommandRoot commandRoot = preparedComTaskExecution.getCommandRoot();
            this.executionContext.setCommandRoot(commandRoot);
            commandRoot.executeFor(preparedComTaskExecution, this.executionContext);
            List<Problem> problems = commandRoot.getProblems();
            if (!problems.isEmpty()) {
                successIndicator = ComTaskExecutionSession.SuccessIndicator.Failure;
                return false;
            } else {
                return true;
            }
        } catch (Throwable t) {
            this.failure(preparedComTaskExecution.getComTaskExecution(), t);
            successIndicator = ComTaskExecutionSession.SuccessIndicator.Failure;
            throw t;
        } finally {
            this.completeExecutedComTask(preparedComTaskExecution.getComTaskExecution(), successIndicator);
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

    /**
     * Notify interested parties that the execution of the {@link ComTaskExecution} failed
     * because of {@link Problem}s that were reported during its execution.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param problems         The List of Problem
     */
    private void failureWithProblems(ComTaskExecution comTaskExecution, List<Problem> problems) {
        this.getExecutionContext().failWithProblems(comTaskExecution, problems);
    }

    public String getThreadName() {
        return Thread.currentThread().getName();
    }

    public void closeConnection() {
        if (getExecutionContext() != null) {
            getExecutionContext().close();
        }
    }

    protected void completeSuccessfulComSession() {
        this.getExecutionContext().complete();
        this.getDeviceCommandExecutor().execute(this.getExecutionContext().getStoreCommand(), getToken());
    }

    protected void completeFailedComSession(Throwable t, ComSession.SuccessIndicator reason) {
        this.getExecutionContext().fail(t, reason);
        this.getDeviceCommandExecutor().execute(this.getExecutionContext().getStoreCommand(), getToken());
    }

    protected ExecutionContext newExecutionContext(ConnectionTask<?, ?> connectionTask, ComPort comPort) {
        return newExecutionContext(connectionTask, comPort, true);
    }

    protected ExecutionContext newExecutionContext(ConnectionTask<?, ?> connectionTask, ComPort comPort, boolean logConnectionProperties) {
        return new ExecutionContext(this, connectionTask, comPort, logConnectionProperties, new ComCommandServiceProvider());
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

    private ComSession.SuccessIndicator getFailureIndicatorFor(RescheduleBehavior.RescheduleReason rescheduleReason) {
        if (RescheduleBehavior.RescheduleReason.CONNECTION_BROKEN.equals(rescheduleReason)) {
            return ComSession.SuccessIndicator.Broken;
        }
        return ComSession.SuccessIndicator.SetupError;
    }

    protected final void doReschedule(final RescheduleBehavior.RescheduleReason rescheduleReason) {
        final RescheduleBehavior retryBehavior = this.getRescheduleBehavior();
        try {
            serviceProvider.transactionService().execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    retryBehavior.performRescheduling(rescheduleReason);
                }
            });
        } catch (BusinessException e) {
            throw CodingException.unexpectedBusinessException(e);
        } catch (SQLException e) {
            throw PersistenceCodingException.unexpectedSqlError(e);
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
                } else {
                    getFailedComTaskExecutions().add(preparedComTaskExecution.getComTaskExecution());
                }
            }
            // if the basicCheck fails, then all other ComTasks will not be executed (except the logOff etc.)
        }
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

    private boolean checkIfWeCanPerformOtherComTaskExecutionsBasedOnConnectionRelatedExceptions(ComServerRuntimeException e) {
        return CommunicationException.class.isAssignableFrom(e.getClass());
    }

    @Override
    public void setToken(DeviceCommandExecutionToken deviceCommandExecutionToken) {
        this.token = deviceCommandExecutionToken;
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
            this.initializeComSessionShadow(logConnectionProperties);
            this.logger = Logger.getAnonymousLogger();  // Start with an anonymous one, refine it later
        }

        private ComServerDAO getComServerDAO() {
            return this.jobExecution.getComServerDAO();
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

        private void initializeComSessionShadow(boolean logConnectionProperties) {
            sessionBuilder = serviceProvider.getTaskHistoryService().buildComSession(connectionTask, connectionTask.getComPortPool(), comPort, now());
//            this.sessionShadow.setSuccessIndicator(ComSession.SuccessIndicator.Success);   // Optimistic
            if (logConnectionProperties && this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
                this.addConnectionPropertiesAsJournalEntries(this.connectionTask);
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

        private void appendPropertyToMessage(StringBuilder builder, ConnectionTaskProperty property) {
            this.appendPropertyToMessage(builder, property.getName(), property.getValue());
        }

        private void addProtocolDialectPropertiesAsJournalEntries(ComTaskExecution comTaskExecution) {
            TypedProperties protocolDialectTypedProperties = getProtocolDialectTypedProperties(comTaskExecution, serviceProvider.getDeviceDataService());
            if (!protocolDialectTypedProperties.propertyNames().isEmpty()) {
                currentTaskExecutionBuilder.addComTaskExecutionMessageJournalEntry(now(), "", asString(protocolDialectTypedProperties));
            }
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

        private void appendPropertyToMessage(StringBuilder builder, String propertyName, Object propertyValue) {
            builder.append(
                    MessageFormat.format(
                            "{0} = {1}",
                            propertyName, String.valueOf(propertyValue))
            );
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

        public void createJournalEntry(String message) {
            if (this.getCurrentTaskExecutionBuilder() != null) {
                this.createComTaskExecutionMessageJournalEntry(message);
            } else {
                this.createComSessionJournalEntry(message);
            }
        }

        private void createComTaskExecutionMessageJournalEntry(String message) {
            getCurrentTaskExecutionBuilder().addComTaskExecutionMessageJournalEntry(now(), "", message);
        }

        private void createComSessionJournalEntry(String message) {
            getComSessionBuilder().addJournalEntry(now(), message, null);
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
         * @see #initializeComSessionShadow(boolean)
         * @see #close()
         * <p/>
         * Note: Arguments are used by AOP
         */
        private void connectionFailed(ConnectionException e, ConnectionTask<?, ?> connectionTask) {
        }

        public JobExecution getJob() {
            return this.jobExecution;
        }

        public Logger getLogger() {
            return logger;
        }

        public void setLogger(Logger logger) {
            this.logger = logger;
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

        public ComPortRelatedComChannel getComChannel() {
            return comChannel;
        }

        private void setComChannel(ComPortRelatedComChannel comChannel) {
            this.comChannel = comChannel;
            this.jobExecution.connected(comChannel);
        }

        public ComSessionBuilder getComSessionBuilder() {
            return sessionBuilder;
        }

        public ComTaskExecutionSessionBuilder getCurrentTaskExecutionBuilder() {
            return currentTaskExecutionBuilder;
        }

        public void start(ComTaskExecution comTaskExecution) {
            this.comTaskExecution = comTaskExecution;
            this.currentTaskExecutionBuilder = sessionBuilder.addComTaskExecutionSession(comTaskExecution, connectionTask.getDevice(), now());
            if (this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
                this.addProtocolDialectPropertiesAsJournalEntries(comTaskExecution);
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

        private Date now() {
            return serviceProvider.getClock().now();
        }

        private List<DeviceCommand> toDeviceCommands(ComTaskExecutionComCommand commandRoot) {
            List<CollectedData> collectedData = commandRoot.getCollectedData();
            List<ServerCollectedData> serverCollectedData = new ArrayList<>(collectedData.size());
            for (CollectedData data : collectedData) {
                serverCollectedData.add((ServerCollectedData) data);
            }
            return new DeviceCommandFactoryImpl().newForAll(serverCollectedData, this.serviceProvider.issueService());
        }

        private void fail(ComTaskExecution comTaskExecution, Throwable t) {
            if (this.currentTaskExecutionBuilder != null) { // Check if the failure occurred in the context of an executing ComTask
                if (!ComServerRuntimeException.class.isAssignableFrom(t.getClass())) {
                    currentTaskExecutionBuilder.addComCommandJournalEntry(now(), CompletionCode.UnexpectedError, t.getLocalizedMessage(), "General");
                }// else the task will be logged, next task can be executed
            }
        }

        public void fail(Throwable t, ComSession.SuccessIndicator reason) {
            sessionBuilder.addJournalEntry(now(), messageFor(t), t);
            this.createComSessionCommand(sessionBuilder.endSession(now(), reason));
        }

        private String messageFor(Throwable t) {
            return t.getMessage() == null ? t.toString() : t.getMessage();
        }

        public void close() {
            // Check if establishing the connection succeeded
            if (this.comChannel != null) {
                this.comChannel.close();
            }
        }

        public void complete() {
            this.sessionBuilder.incrementFailedTasks(this.jobExecution.getFailedComTaskExecutions().size());
            this.sessionBuilder.incrementSuccessFulTasks(this.jobExecution.getSuccessfulComTaskExecutions().size());
            this.sessionBuilder.incrementNotExecutedTasks(this.jobExecution.getNotExecutedComTaskExecutions().size());
            ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder;
            if (Thread.currentThread().isInterrupted()) {
                endedComSessionBuilder = sessionBuilder.endSession(now(), ComSession.SuccessIndicator.Broken);
            } else {
                endedComSessionBuilder = sessionBuilder.endSession(now(), ComSession.SuccessIndicator.Success);
            }
            this.createComSessionCommand(endedComSessionBuilder);
        }

        private void createComSessionCommand(ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder) {
            if (this.connectionTask instanceof OutboundConnectionTask) {
                this.getStoreCommand().
                        add(new CreateOutboundComSession(
                                this.comPort.getComServer().getCommunicationLogLevel(),
                                (ScheduledConnectionTask) this.connectionTask,
                                endedComSessionBuilder));
            } else {
                this.getStoreCommand().
                        add(new CreateInboundComSession(
                                (InboundComPort) getComPort(),
                                (InboundConnectionTask) this.connectionTask,
                                endedComSessionBuilder));
            }
        }

        public CommandRoot getCommandRoot() {
            return commandRoot;
        }

        public void setCommandRoot(CommandRoot commandRoot) {
            this.commandRoot = commandRoot;
        }

        public CompositeDeviceCommand getStoreCommand() {
            if (this.storeCommand == null) {
                this.storeCommand = new ComSessionRootDeviceCommand(this.getComPort().getComServer().getCommunicationLogLevel());
            }
            return this.storeCommand;
        }

        public ComCommandJournalist getJournalist() {
            return journalist;
        }

        public void initializeJournalist() {
            this.journalist = new ComCommandJournalist(this.getCurrentTaskExecutionBuilder());
        }

        public ComCommandLogger getComCommandLogger() {
            return comCommandLogger;
        }

        public void setComCommandLogger(ComCommandLogger comCommandLogger) {
            this.comCommandLogger = comCommandLogger;
        }

        public ComChannelLogger getComChannelLogger() {
            return comChannelLogger;
        }

        public void setComChannelLogger(ComChannelLogger comChannelLogger) {
            this.comChannelLogger = comChannelLogger;
        }

        public void markComTaskExecutionForConnectionSetupError(String reason) {
            if (this.currentTaskExecutionBuilder != null) { // Check if the failure occurred in the context of an executing ComTask
                currentTaskExecutionBuilder.addComCommandJournalEntry(now(), CompletionCode.ConnectionError, reason, "ConnectionType - Connect");
            }
        }

        public void setComSessionShadow(ComSessionBuilder comSessionShadow) {
            this.sessionBuilder = comSessionShadow;
        }

        public boolean hasBasicCheckFailed() {
            return basicCheckFailed;
        }

        public void setBasicCheckFailed(boolean basicCheckFailed) {
            this.basicCheckFailed = basicCheckFailed;
        }

        public void failForRetryAsapComTaskExec(ComTaskExecution notExecutedComTaskExecution, Throwable t) {
            fail(notExecutedComTaskExecution, t);
            comTaskExecutionCompleted(ComTaskExecutionSession.SuccessIndicator.Success);
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


    private CommandRoot.ServiceProvider getComCommandServiceProvider() {
        return new ComCommandServiceProvider();
    }

    private class ComCommandServiceProvider implements CommandRoot.ServiceProvider {

        @Override
        public IssueService getIssueService() {
            return JobExecution.this.serviceProvider.issueService();
        }

        @Override
        public Clock getClock() {
            return JobExecution.this.serviceProvider.clock();
        }

        @Override
        public DeviceDataService getDeviceDataService() {
            return JobExecution.this.serviceProvider.deviceDataService();
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

        public OfflineDevice getOfflineDevice() {
            return offlineDevice;
        }

        public CommandRoot getRoot() {
            return root;
        }

        public void setRoot(CommandRoot root) {
            this.root = root;
        }

        public DeviceProtocol getDeviceProtocol() {
            return deviceProtocol;
        }

        public CommandCreator getCommandCreator() {
            return commandCreator;
        }

        public ComTaskPreparationContext invoke() {
            this.takeDeviceOffline();
            root = new CommandRootImpl(offlineDevice, getExecutionContext(), getComCommandServiceProvider());
            DeviceProtocolPluggableClass protocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
            deviceProtocol = protocolPluggableClass.getDeviceProtocol();
            commandCreator = CommandFactory.commandCreatorForPluggableClass(protocolPluggableClass);
            return this;
        }

        private void takeDeviceOffline() {
            final OfflineDeviceForComTaskGroup offlineDeviceForComTaskGroup = new OfflineDeviceForComTaskGroup(deviceOrganizedComTaskExecution.getComTaskExecutions());
            offlineDevice = (OfflineDevice) deviceOrganizedComTaskExecution.getDevice().goOffline(offlineDeviceForComTaskGroup);
        }
    }

    private class PrepareAllTransaction implements Transaction<List<PreparedComTaskExecution>> {

        private final List<? extends ComTaskExecution> comTaskExecutions;

        private PrepareAllTransaction(List<? extends ComTaskExecution> comTaskExecutions) {
            this.comTaskExecutions = comTaskExecutions;
        }

        @Override
        public List<PreparedComTaskExecution> perform() {
            List<PreparedComTaskExecution> prepared = new ArrayList<>();
            List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                    ComTaskExecutionOrganizer.
                            defineComTaskExecutionOrders(
                                    comTaskExecutions.toArray(new ComTaskExecution[comTaskExecutions.size()]));
            for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : deviceOrganizedComTaskExecutions) {
                ComTaskPreparationContext comTaskPreparationContext = new ComTaskPreparationContext(deviceOrganizedComTaskExecution).invoke();
                for (DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps comTaskWithSecurityAndConnectionSteps : deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity()) {
                    final ComTaskExecution comTaskExecution = comTaskWithSecurityAndConnectionSteps.getComTaskExecution();
                    final ComTaskExecutionConnectionSteps connectionSteps = comTaskWithSecurityAndConnectionSteps.getComTaskExecutionConnectionSteps();
                    final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet();
                    final PreparedComTaskExecution preparedComTaskExecution =
                            getPreparedComTaskExecution(
                                    comTaskPreparationContext,
                                    comTaskExecution,
                                    connectionSteps,
                                    deviceOrganizedComTaskExecution.getDevice(),
                                    deviceProtocolSecurityPropertySet);
                    prepared.add(preparedComTaskExecution);
                }
                //GenericDeviceProtocols can reorganize the commands
                if (GenericDeviceProtocol.class.isAssignableFrom(comTaskPreparationContext.getDeviceProtocol().getClass())) {
                    comTaskPreparationContext.setRoot(((GenericDeviceProtocol) comTaskPreparationContext.getDeviceProtocol()).organizeComCommands(comTaskPreparationContext.getRoot()));
                }
            }
            return prepared;
        }
    }

    private class PrepareTransaction implements Transaction<PreparedComTaskExecution> {

        private final ComTaskExecution comTaskExecution;

        private PrepareTransaction(ComTaskExecution comTaskExecution) {
            super();
            this.comTaskExecution = comTaskExecution;
        }

        @Override
        public PreparedComTaskExecution perform() {
            final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = ComTaskExecutionOrganizer.defineComTaskExecutionOrders(comTaskExecution);
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
    }
}