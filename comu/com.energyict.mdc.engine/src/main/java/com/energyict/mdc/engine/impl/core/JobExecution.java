package com.energyict.mdc.engine.impl.core;

import com.energyict.comserver.GenericDeviceProtocol;
import com.energyict.comserver.aspects.journaling.ComCommandJournalist;
import com.energyict.comserver.aspects.logging.ComChannelLogger;
import com.energyict.comserver.aspects.logging.ComCommandLogger;
import com.energyict.comserver.commands.ComSessionRootDeviceCommand;
import com.energyict.comserver.commands.CompositeDeviceCommand;
import com.energyict.comserver.commands.CreateInboundComSession;
import com.energyict.comserver.commands.CreateOutboundComSession;
import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.comserver.commands.DeviceCommandExecutionToken;
import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.comserver.commands.DeviceCommandFactoryImpl;
import com.energyict.comserver.commands.core.ComTaskExecutionComCommand;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.time.Clocks;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.communication.tasks.OfflineDeviceForComTaskGroup;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.device.data.journal.ComTaskExecutionSession;
import com.energyict.mdc.device.data.journal.CompletionCode;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.exceptions.PersistenceCodingException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.ComChannelPlaceHolder;
import com.energyict.mdc.protocol.ComPortRelatedComChannel;
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
import com.energyict.mdc.shadow.journal.ComCommandJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComSessionJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComSessionShadow;
import com.energyict.mdc.shadow.journal.ComStatisticsShadow;
import com.energyict.mdc.shadow.journal.ComTaskExecutionMessageJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComTaskExecutionSessionShadow;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdw.core.TransactionExecutorProvider;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    private final IssueService issueService;
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

    public abstract ConnectionTask getConnectionTask();

    public JobExecution(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, IssueService issueService) {
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.issueService = issueService;
        this.preparationContext = new PreparationContext();
    }

    public ComPort getComPort() {
        return comPort;
    }

    protected ComServerDAO getComServerDAO() {
        return comServerDAO;
    }

    protected PreparedComTaskExecution getPreparedComTaskExecution(ComTaskPreparationContext comTaskPreparationContext, ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, BaseDevice<?,?,?> masterDevice, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        final List<? extends ProtocolTask> protocolTasks = comTaskExecution.getComTask().getProtocolTasks();
        makeSureBasicCheckTaskIsInFrontOfTheList(protocolTasks);
        comTaskPreparationContext.getCommandCreator().
                createCommands(
                        comTaskPreparationContext.getRoot(),
                        getProtocolDialectTypedProperties(comTaskExecution),
                        this.preparationContext.getComChannelPlaceHolder(),
                        comTaskPreparationContext.getOfflineDevice(),
                        protocolTasks,
                        deviceProtocolSecurityPropertySet,
                        connectionSteps, comTaskExecution, issueService);
        return new PreparedComTaskExecution(comTaskExecution, comTaskPreparationContext.getRoot(), comTaskPreparationContext.getDeviceProtocol());
    }

    private static TypedProperties getProtocolDialectTypedProperties(ComTaskExecution comTaskExecution) {
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = comTaskExecution.getProtocolDialectConfigurationProperties();
        ProtocolDialectProperties protocolDialectPropertiesWithName = ManagerFactory.getCurrent().getProtocolDialectPropertiesFactory().findProtocolDialectPropertiesWithName(protocolDialectConfigurationProperties.getDeviceProtocolDialectName(), (int) comTaskExecution.getDevice().getId());
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
        try {
           return TransactionExecutorProvider.instance.get().getTransactionExecutor().execute(new Transaction<List<PreparedComTaskExecution>>() {
               @Override
               public List<PreparedComTaskExecution> doExecute() throws BusinessException, SQLException {
                   getExecutionContext().getComSessionShadow().setNumberOfPlannedButNotExecutedTasks(comTaskExecutions.size());
                   List<PreparedComTaskExecution> prepared = new ArrayList<>();
                   final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = ComTaskExecutionOrganizer.defineComTaskExecutionOrders(comTaskExecutions.toArray(new ComTaskExecution[comTaskExecutions.size()]));
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
           });
        } catch (BusinessException e) {
            throw CodingException.unexpectedBusinessException(e);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }finally{
            Environment.DEFAULT.get().closeConnection();
        }
    }

    protected PreparedComTaskExecution prepareOne(final ComTaskExecution comTaskExecution) {
        try {
            return TransactionExecutorProvider.instance.get().getTransactionExecutor().execute(new Transaction<PreparedComTaskExecution>() {
                @Override
                public PreparedComTaskExecution doExecute() throws BusinessException, SQLException {
                    getExecutionContext().getComSessionShadow().setNumberOfPlannedButNotExecutedTasks(1);
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
            });
        } catch (BusinessException e) {
            throw CodingException.unexpectedBusinessException(e);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }finally{
            Environment.DEFAULT.get().closeConnection();
        }
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
        try {
            this.start(preparedComTaskExecution.getComTaskExecution());

            CommandRoot commandRoot = preparedComTaskExecution.getCommandRoot();
            this.executionContext.setCommandRoot(commandRoot);
            commandRoot.executeFor(preparedComTaskExecution, this.executionContext);
            List<Problem> problems = commandRoot.getProblems();
            if (!problems.isEmpty()) {
                this.failureWithProblems(preparedComTaskExecution.getComTaskExecution(), problems);
                return false;
            }
            else {
                return true;
            }
        } catch (Throwable t) {
            this.failure(preparedComTaskExecution.getComTaskExecution(), t);
            throw t;
        } finally {
            this.completeExecutedComTask(preparedComTaskExecution.getComTaskExecution());
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
     */
    private void completeExecutedComTask(ComTaskExecution comTaskExecution) {
        this.getExecutionContext().comTaskExecutionCompleted();
    }

    /**
     * Notify interested parties that the execution of the {@link ComTaskExecution} failed
     * because of an exceptional situation that was not necessarily anticipated.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param t The failure
     */
    private void failure(ComTaskExecution comTaskExecution, Throwable t) {
        this.getExecutionContext().fail(comTaskExecution, t);
    }

    /**
     * Notify interested parties that the execution of the {@link ComTaskExecution} failed
     * because of {@link Problem}s that were reported during its execution.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param problems The List of Problem
     */
    private void failureWithProblems (ComTaskExecution comTaskExecution, List<Problem> problems) {
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

    protected void completeFailedComSession (Throwable t, ComSession.SuccessIndicator reason) {
        this.getExecutionContext().fail(t, reason);
        this.getDeviceCommandExecutor().execute(this.getExecutionContext().getStoreCommand(), getToken());
    }

    protected ExecutionContext newExecutionContext(ConnectionTask connectionTask, ComPort comPort) {
        return newExecutionContext(connectionTask, comPort, true);
    }

    protected ExecutionContext newExecutionContext(ConnectionTask connectionTask, ComPort comPort, boolean logConnectionProperties) {
        return new ExecutionContext(this, connectionTask, comPort, logConnectionProperties, issueService);
    }

    @Override
    public void reschedule () {
        this.doReschedule(RescheduleBehavior.RescheduleReason.COMTASKS);
        this.completeSuccessfulComSession();
    }

    @Override
    public void reschedule (Throwable t, RescheduleBehavior.RescheduleReason rescheduleReason) {
        this.doReschedule(rescheduleReason);
        this.completeFailedComSession(t, this.getFailureIndicatorFor(rescheduleReason));
    }

    private ComSession.SuccessIndicator getFailureIndicatorFor (RescheduleBehavior.RescheduleReason rescheduleReason) {
        if (RescheduleBehavior.RescheduleReason.CONNECTION_BROKEN.equals(rescheduleReason)) {
            return ComSession.SuccessIndicator.Broken;
        }
        else {
            return ComSession.SuccessIndicator.SetupError;
        }
    }

    protected final void doReschedule (final RescheduleBehavior.RescheduleReason rescheduleReason) {
        final RescheduleBehavior retryBehavior = this.getRescheduleBehavior();
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() throws BusinessException, SQLException {
                    retryBehavior.performRescheduling(rescheduleReason);
                    return null;
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
            if(checkIfWeCanPerformOtherComTaskExecutionsBasedOnConnectionRelatedExceptions(e)){
                throw e;
            } else if(checkWhetherAllNextComTasksShouldNotBeExecutedBasedonBasicCheckFailure(e)) {
                getExecutionContext().setBasicCheckFailed(true);
            }
            // else the task will be logged, next task can be executed
        }
    }

    private boolean checkWhetherAllNextComTasksShouldNotBeExecutedBasedonBasicCheckFailure(ComServerRuntimeException e) {
        if(DeviceConfigurationException.class.isAssignableFrom(e.getClass())){
            switch (e.getMessageId()){
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

    private enum ListAppendMode {
        FIRST {
            @Override
            protected void startOn (StringBuilder builder) {
                // Nothing to append before the first message
            }
        },
        REMAINING {
            @Override
            protected void startOn (StringBuilder builder) {
                builder.append(", ");
            }
        };

        protected abstract void startOn (StringBuilder builder);

    }

    public static final class ExecutionContext {

        private final JobExecution jobExecution;
        private final IssueService issueService;

        private ComPortRelatedComChannel comChannel;
        private ComSessionShadow sessionShadow = new ComSessionShadow();
        private ComTaskExecutionSessionShadow currentTaskExecutionSession;
        private ComCommandJournalist journalist;
        private ComCommandLogger comCommandLogger;
        private ComChannelLogger comChannelLogger;
        private ComPort comPort;
        private ConnectionTask connectionTask;
        private ComTaskExecution comTaskExecution;
        private Logger logger;

        private CommandRoot commandRoot;
        private CompositeDeviceCommand storeCommand;
        private boolean basicCheckFailed = false;

        public ExecutionContext(JobExecution jobExecution, ConnectionTask connectionTask, ComPort comPort, IssueService issueService) {
            this(jobExecution, connectionTask, comPort, true, issueService);
        }

        public ExecutionContext(JobExecution jobExecution, ConnectionTask connectionTask, ComPort comPort, boolean logConnectionProperties, IssueService issueService) {
            super();
            this.jobExecution = jobExecution;
            this.comPort = comPort;
            this.connectionTask = connectionTask;
            this.issueService = issueService;
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
                this.getComSessionShadow().setSuccessIndicator(ComSession.SuccessIndicator.Success);
                this.getComServerDAO().executionStarted(this.connectionTask, this.comPort.getComServer());
                return this.jobExecution.isConnected();
            } catch (ConnectionException e) {
                this.comChannel = null;
                this.connectionFailed(e, this.connectionTask);
                throw new ConnectionSetupException(e);
            }
        }

        private void initializeComSessionShadow (boolean logConnectionProperties) {
            this.sessionShadow.setStartDate(Clocks.getAppServerClock().now());
            this.sessionShadow.setConnectionTaskId((int) this.connectionTask.getId());
            this.sessionShadow.setComPortId((int) this.comPort.getId()); // TODO replace with long
            this.sessionShadow.setComPortPoolId((int) this.connectionTask.getComPortPool().getId());// TODO replace with long
            this.sessionShadow.setSuccessIndicator(ComSession.SuccessIndicator.Success);   // Optimistic
            if (logConnectionProperties && this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
                this.addConnectionPropertiesAsJournalEntries(this.connectionTask);
            }
        }

        private void addConnectionPropertiesAsJournalEntries(ConnectionTask connectionTask) {
            List<ConnectionTaskProperty> connectionProperties = connectionTask.getProperties();
            if (!connectionProperties.isEmpty()) {
                ComSessionJournalEntryShadow shadow = new ComSessionJournalEntryShadow();
                shadow.setTimestamp(Clocks.getAppServerClock().now());
                StringBuilder builder = new StringBuilder("Connection properties: ");
                ListAppendMode appendMode = ListAppendMode.FIRST;
                for (ConnectionTaskProperty connectionProperty : connectionProperties) {
                    appendMode.startOn(builder);
                    this.appendPropertyToMessage(builder, connectionProperty);
                    appendMode = ListAppendMode.REMAINING;
                }
                shadow.setMessage(builder.toString());
                this.sessionShadow.addJournaleEntry(shadow);
            }
        }

        private void appendPropertyToMessage (StringBuilder builder, ConnectionTaskProperty property) {
            this.appendPropertyToMessage(builder, property.getName(), property.getValue());
        }

        private void addProtocolDialectPropertiesAsJournalEntries(ComTaskExecution comTaskExecution) {
            TypedProperties protocolDialectTypedProperties = getProtocolDialectTypedProperties(comTaskExecution);
            if (!protocolDialectTypedProperties.propertyNames().isEmpty()) {
                ComTaskExecutionMessageJournalEntryShadow shadow = new ComTaskExecutionMessageJournalEntryShadow();
                shadow.setTimestamp(Clocks.getAppServerClock().now());
                StringBuilder builder = new StringBuilder("Protocol dialect properties: ");
                ListAppendMode appendMode = ListAppendMode.FIRST;
                for (String propertyName : protocolDialectTypedProperties.propertyNames()) {
                    appendMode.startOn(builder);
                    this.appendPropertyToMessage(builder, propertyName, protocolDialectTypedProperties.getProperty(propertyName));
                    appendMode = ListAppendMode.REMAINING;
                }
                shadow.setMessage(builder.toString());
                this.currentTaskExecutionSession.addComTaskJournalEntry(shadow);
            }
        }

        private void appendPropertyToMessage (StringBuilder builder, String propertyName, Object propertyValue) {
            builder.append(
                    MessageFormat.format(
                            "{0} = {1}",
                            propertyName, String.valueOf(propertyValue)));
        }

        /**
         * Tests if the LogLevel is enabled,
         * in which case something should be logged in the ComSession.
         *
         * @param logLevel The ComServer.LogLevel
         * @return A flag that indicates if the DeviceCommand should be logged
         */
        private boolean isLogLevelEnabled (ComServer.LogLevel logLevel) {
            return this.comPort.getComServer().getCommunicationLogLevel().compareTo(logLevel) >= 0;
        }

        public void createJournalEntry(String message) {
            if (this.getCurrentTaskExecutionSession() != null) {
                this.createComTaskExecutionMessageJournalEntry(message);
            } else {
                this.createComSessionJournalEntry(message);
            }
        }

        private void createComTaskExecutionMessageJournalEntry(String message) {
            ComTaskExecutionMessageJournalEntryShadow shadow = new ComTaskExecutionMessageJournalEntryShadow();
            shadow.setTimestamp(Clocks.getAppServerClock().now());
            shadow.setMessage(message);
            this.getCurrentTaskExecutionSession().addComTaskJournalEntry(shadow);
        }

        private void createComSessionJournalEntry(String message) {
            ComSessionJournalEntryShadow shadow = new ComSessionJournalEntryShadow();
            shadow.setTimestamp(Clocks.getAppServerClock().now());
            shadow.setMessage(message);
            this.getComSessionShadow().addJournaleEntry(shadow);
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
         *      <p/>
         *      Note: Arguments are used by AOP
         */
        private void connectionFailed(ConnectionException e, ConnectionTask connectionTask) {
            this.zeroOutTimings();
            this.cleanStatistics();
        }

        private void zeroOutTimings() {
            this.sessionShadow.setConnectMillis(0);
            this.sessionShadow.setTalkMillis(0);
            this.sessionShadow.setStoreMillis(0);
        }

        private void cleanStatistics() {
            this.clean(this.sessionShadow.getComStatistics());
        }

        private void clean(ComStatisticsShadow shadow) {
            shadow.setNrOfBytesSent(0);
            shadow.setNrOfBytesRead(0);
            shadow.setNrOfPacketsRead(0);
            shadow.setNrOfPacketsSent(0);
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

        public ConnectionTask getConnectionTask() {
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

        public ComSessionShadow getComSessionShadow() {
            return sessionShadow;
        }

        public ComTaskExecutionSessionShadow getCurrentTaskExecutionSession() {
            return currentTaskExecutionSession;
        }

        public void start(ComTaskExecution comTaskExecution) {
            this.comTaskExecution = comTaskExecution;
            this.currentTaskExecutionSession = new ComTaskExecutionSessionShadow();
            this.currentTaskExecutionSession.setComTaskExecutionId((int) comTaskExecution.getId());
            this.currentTaskExecutionSession.setSuccessIndicator(ComTaskExecutionSession.SuccessIndicator.Success);   // Optimistic
            this.currentTaskExecutionSession.setStartDate(Clocks.getAppServerClock().now());
            this.currentTaskExecutionSession.setDeviceId((int) this.connectionTask.getDevice().getId());
            this.sessionShadow.addComTaskSession(this.currentTaskExecutionSession);
            if (this.isLogLevelEnabled(ComServer.LogLevel.DEBUG)) {
                this.addProtocolDialectPropertiesAsJournalEntries(comTaskExecution);
            }
        }

        public void comTaskExecutionCompleted() {
            if (hasBasicCheckFailed()) {
                ComCommandJournalEntryShadow comCommandJournalEntryShadow = new ComCommandJournalEntryShadow();
                comCommandJournalEntryShadow.setCompletionCode(CompletionCode.Rescheduled);
                comCommandJournalEntryShadow.setTimestamp(Clocks.getAppServerClock().now());
                comCommandJournalEntryShadow.setCommandDescription("ComTask will be rescheduled due to the failure of the BasicCheck task.");
                this.currentTaskExecutionSession.addComTaskJournalEntry(comCommandJournalEntryShadow);
            }
            this.currentTaskExecutionSession.setStopDate(Clocks.getAppServerClock().now());
            this.currentTaskExecutionSession = null;
            this.getStoreCommand().addAll(this.toDeviceCommands(this.getCommandRoot().getComTaskRoot(this.comTaskExecution)));
        }

        private List<DeviceCommand> toDeviceCommands(ComTaskExecutionComCommand commandRoot) {
            List<CollectedData> collectedData = commandRoot.getCollectedData();
            List<ServerCollectedData> serverCollectedData = new ArrayList<>(collectedData.size());
            for (CollectedData data : collectedData) {
                serverCollectedData.add((ServerCollectedData) data);
            }
            return new DeviceCommandFactoryImpl().newForAll(serverCollectedData, this.issueService);
        }

        public void fail(ComTaskExecution comTaskExecution, Throwable t) {
            if (this.currentTaskExecutionSession != null) { // Check if the failure occurred in the context of an executing ComTask
                if (!ComServerRuntimeException.class.isAssignableFrom(t.getClass())) {
                    ComCommandJournalEntryShadow comCommandJournalEntryShadow = new ComCommandJournalEntryShadow();
                    comCommandJournalEntryShadow.setCompletionCode(CompletionCode.UnexpectedError);
                    comCommandJournalEntryShadow.setErrorDescription(t.getLocalizedMessage());
                    comCommandJournalEntryShadow.setTimestamp(Clocks.getAppServerClock().now());
                    comCommandJournalEntryShadow.setCommandDescription("General");
                    this.currentTaskExecutionSession.addComTaskJournalEntry(comCommandJournalEntryShadow);
                }// else the task will be logged, next task can be executed
                this.currentTaskExecutionSession.setStopDate(Clocks.getAppServerClock().now());
                this.currentTaskExecutionSession.setSuccessIndicator(ComTaskExecutionSession.SuccessIndicator.Failure);
            }
        }

        public void failWithProblems (ComTaskExecution comTaskExecution, List<Problem> problems) {
            if (this.currentTaskExecutionSession != null) { // Check if the failure occurred in the context of an executing ComTask
                this.currentTaskExecutionSession.setStopDate(Clocks.getAppServerClock().now());
                this.currentTaskExecutionSession.setSuccessIndicator(ComTaskExecutionSession.SuccessIndicator.Failure);
            }
        }

        public void fail(Throwable t, ComSession.SuccessIndicator reason) {
            this.sessionShadow.setStopDate(Clocks.getAppServerClock().now());
            this.sessionShadow.setSuccessIndicator(reason);
            ComSessionJournalEntryShadow comSessionJournalEntryShadow = new ComSessionJournalEntryShadow();
            comSessionJournalEntryShadow.setTimestamp(Clocks.getAppServerClock().now());
            setMessageAndCause(t, comSessionJournalEntryShadow);
            this.sessionShadow.addJournaleEntry(comSessionJournalEntryShadow);
            this.createComSessionCommand();
        }

        private void setMessageAndCause(Throwable t, ComSessionJournalEntryShadow comSessionJournalEntryShadow) {
            if(NullPointerException.class.isAssignableFrom(t.getClass())){
                /* In case of a NullPointer we don't have any message */
                comSessionJournalEntryShadow.setMessage("An exception occurred...");
            } else {
                comSessionJournalEntryShadow.setMessage(t.getMessage());
            }
            comSessionJournalEntryShadow.setCause(t);
        }

        public void close() {
            // Check if establishing the connection succeeded
            if (this.comChannel != null) {
                this.comChannel.close();
            }
        }

        public void complete() {
            this.sessionShadow.setNumberOfFailedTasks(this.jobExecution.getFailedComTaskExecutions().size());
            this.sessionShadow.setNumberOfSuccessFulTasks(this.jobExecution.getSuccessfulComTaskExecutions().size());
            this.sessionShadow.setNumberOfPlannedButNotExecutedTasks(this.jobExecution.getNotExecutedComTaskExecutions().size());
            this.sessionShadow.setStopDate(Clocks.getAppServerClock().now());
            if (Thread.currentThread().isInterrupted()) {
                this.sessionShadow.setSuccessIndicator(ComSession.SuccessIndicator.Broken);
            }
            this.createComSessionCommand();
        }

        private void createComSessionCommand() {
            if (this.connectionTask instanceof OutboundConnectionTask) {
                this.getStoreCommand().
                    add(new CreateOutboundComSession(
                            this.comPort.getComServer().getCommunicationLogLevel(),
                            (ScheduledConnectionTask) this.connectionTask,
                            this.sessionShadow));
            } else {
                this.getStoreCommand().
                    add(new CreateInboundComSession(
                            (InboundComPort) getComPort(),
                            (InboundConnectionTask) this.connectionTask,
                            this.sessionShadow));
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
            this.journalist = new ComCommandJournalist(this.getCurrentTaskExecutionSession());
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
            if (this.currentTaskExecutionSession != null) { // Check if the failure occurred in the context of an executing ComTask
                ComCommandJournalEntryShadow comCommandJournalEntryShadow = new ComCommandJournalEntryShadow();
                comCommandJournalEntryShadow.setCompletionCode(CompletionCode.ConnectionError);
                comCommandJournalEntryShadow.setErrorDescription(reason);
                comCommandJournalEntryShadow.setTimestamp(Clocks.getAppServerClock().now());
                comCommandJournalEntryShadow.setCommandDescription("ConnectionType - Connect");
                this.currentTaskExecutionSession.addComTaskJournalEntry(comCommandJournalEntryShadow);
            }
        }

        public void setComSessionShadow(ComSessionShadow comSessionShadow) {
            this.sessionShadow = comSessionShadow;
        }

        public boolean hasBasicCheckFailed() {
            return basicCheckFailed;
        }

        public void setBasicCheckFailed(boolean basicCheckFailed) {
            this.basicCheckFailed = basicCheckFailed;
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
            root = new CommandRootImpl(offlineDevice, getExecutionContext(), issueService);
            DeviceProtocolPluggableClass protocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
            deviceProtocol = protocolPluggableClass.getDeviceProtocol();
            commandCreator = CommandFactory.commandCreatorForPluggableClass(protocolPluggableClass);
            return this;
        }

        private void takeDeviceOffline () {
            final OfflineDeviceForComTaskGroup offlineDeviceForComTaskGroup = new OfflineDeviceForComTaskGroup(deviceOrganizedComTaskExecution.getComTaskExecutions());
            offlineDevice = (OfflineDevice) deviceOrganizedComTaskExecution.getDevice().goOffline(offlineDeviceForComTaskGroup);
        }
    }

}