package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.GenericDeviceProtocol;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.OfflineDeviceForComTaskGroup;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ProtocolTask;

import com.elster.jupiter.transaction.Transaction;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        retryBehavior.performRescheduling(rescheduleReason);
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

    void connected(ComPortRelatedComChannel comChannel) {
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
        final List<? extends ProtocolTask> protocolTasks = new ArrayList<>(comTaskExecution.getComTask().getProtocolTasks()); // copied the ImmutableList
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

    static TypedProperties getProtocolDialectTypedProperties(ComTaskExecution comTaskExecution, DeviceDataService deviceDataService) {
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

        @Override
        public TransactionService transactionService() {
            return JobExecution.this.serviceProvider.transactionService();
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