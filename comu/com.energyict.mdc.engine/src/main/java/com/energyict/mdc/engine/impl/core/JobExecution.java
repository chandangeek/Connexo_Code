package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.GenericDeviceProtocol;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.OfflineDeviceForComTaskGroup;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.PublishConnectionCompletionEvent;
import com.energyict.mdc.engine.impl.commands.store.RescheduleFailedExecution;
import com.energyict.mdc.engine.impl.commands.store.RescheduleSuccessfulExecution;
import com.energyict.mdc.engine.impl.commands.store.UnlockScheduledJobDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.SerialNumberMismatchException;
import com.energyict.mdc.protocol.api.exceptions.TimeDifferenceExceededException;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.mdc.device.data.tasks.history.ComSession.SuccessIndicator.Broken;
import static com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession.SuccessIndicator.Failure;
import static com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession.SuccessIndicator.Success;

/**
 * Provides code reuse for in- and outbound {@link com.energyict.mdc.engine.config.ComPort ComPorts }
 * which perform one or more ComTasks.
 * It will be useful to group the AOP logging as well.
 * <p>
 * Copyrights EnergyICT
 * Date: 25/10/12
 * Time: 16:27
 */
public abstract class JobExecution implements ScheduledJob {

    public interface ServiceProvider extends ExecutionContext.ServiceProvider {

        public DeviceConfigurationService deviceConfigurationService();

        public ConnectionTaskService connectionTaskService();

        public LogBookService logBookService();

        public DeviceService deviceService();

        public TopologyService topologyService();

        public EngineService engineService();

        public MdcReadingTypeUtilService mdcReadingTypeUtilService();

        public HexService hexService();

        public TransactionService transactionService();

        public EventService eventService();

        public IdentificationService identificationService();

        public MeteringService meteringService();

    }

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
    public void completed() {
        this.addCompletionEvent();
        this.completeSuccessfulComSession();
    }

    @Override
    public void reschedule(ComServerDAO comServerDAO) {
        this.doReschedule(comServerDAO, RescheduleBehavior.RescheduleReason.COMTASKS);
    }

    @Override
    public void failed(Throwable t, ExecutionFailureReason reason) {
        this.addCompletionEvent();
        this.completeFailedComSession(t, reason);
    }

    @Override
    public void reschedule(ComServerDAO comServerDAO, Throwable t, RescheduleBehavior.RescheduleReason rescheduleReason) {
        this.getExecutionContext().reschedule(this, this.getFailedComTaskExecutions());
        this.doReschedule(comServerDAO, rescheduleReason);
    }

    private void addCompletionEvent() {
        if (!this.getExecutionContext().connectionFailed()) {
            this.getExecutionContext().getStoreCommand().add(
                    new PublishConnectionCompletionEvent(
                            this.getConnectionTask(),
                            this.getComPort(),
                            this.getSuccessfulComTaskExecutions(),
                            this.getFailedComTaskExecutions(),
                            this.getNotExecutedComTaskExecutions(),
                            this.executionContext.getDeviceCommandServiceProvider()));
        }
    }

    @Override
    public void setToken(DeviceCommandExecutionToken deviceCommandExecutionToken) {
        this.token = deviceCommandExecutionToken;
    }

    protected void completeFailedComSession(Throwable t, ExecutionFailureReason reason) {
        this.getExecutionContext().fail(t, this.getFailureIndicatorFor(reason.toRescheduleReason()));
        this.getExecutionContext().getStoreCommand().add(new RescheduleFailedExecution(this, t, reason));
        this.getExecutionContext().getStoreCommand().add(new UnlockScheduledJobDeviceCommand(this, this.executionContext.getDeviceCommandServiceProvider()));
        doExecuteStoreCommand();
    }

    protected void completeSuccessfulComSession() {
        this.getExecutionContext().complete();
        this.getExecutionContext().getStoreCommand().add(new RescheduleSuccessfulExecution(this));
        this.getExecutionContext().getStoreCommand().add(new UnlockScheduledJobDeviceCommand(this, this.executionContext.getDeviceCommandServiceProvider()));
        doExecuteStoreCommand();
    }

    /**
     * Store all the collected data and create a ComSession
     */
    protected Future<Boolean> doExecuteStoreCommand() {
        return this.getDeviceCommandExecutor().execute(this.getExecutionContext().getStoreCommand(), getToken());
    }

    protected final void doReschedule(ComServerDAO comServerDAO, RescheduleBehavior.RescheduleReason rescheduleReason) {
        this.getRescheduleBehavior(comServerDAO).performRescheduling(rescheduleReason);
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
            if (!getExecutionContext().basickCheckHasFailed()) {
                getNotExecutedComTaskExecutions().remove(preparedComTaskExecution.getComTaskExecution());
                int actualNumberOfTasks = preparedComTaskExecution.getComTaskExecution().getComTasks().size();
                if (success) {
                    getSuccessfulComTaskExecutions().add(preparedComTaskExecution.getComTaskExecution());
                    getExecutionContext().getComSessionBuilder().incrementSuccessFulTasks(actualNumberOfTasks);
                } else {
                    getFailedComTaskExecutions().add(preparedComTaskExecution.getComTaskExecution());
                    getExecutionContext().getComSessionBuilder().incrementFailedTasks(actualNumberOfTasks);
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
    protected abstract ComPortRelatedComChannel findOrCreateComChannel(ConnectionTaskPropertyProvider connectionTaskPropertyProvider) throws ConnectionException;

    protected DeviceCommandExecutor getDeviceCommandExecutor() {
        return deviceCommandExecutor;
    }

    /**
     * Indicates whether a connection has been set-up.
     *
     * @return true if the connection is up, false otherwise
     */
    protected abstract boolean isConnected();

    protected ExecutionContext newExecutionContext(ConnectionTask<?, ?> connectionTask, ComPort comPort, boolean logConnectionProperties) {
        return new ExecutionContext(this, connectionTask, comPort, logConnectionProperties, this.serviceProvider);
    }

    protected void performPreparedComTaskExecution(PreparedComTaskExecution preparedComTaskExecution) {
        try {
            /* No need to attempt to lock the ScheduledComTask
             * as we are locking on the ConnectionTask. */
            this.executeAndUpdateSuccessRate(preparedComTaskExecution);
        } catch (ComServerRuntimeException e) {
            if (checkIfWeCanPerformOtherComTaskExecutionsBasedOnConnectionRelatedExceptions(e)) {
                throw e;
            } else if (checkWhetherAllNextComTasksShouldNotBeExecutedBasedOnBasicCheckFailure(e)) {
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
            boolean noProblems = executionContext.getCommandRoot().getProblems().isEmpty();
            if (noProblems) {
                successIndicator = Success;
            }
            else {
                successIndicator = Failure;
                this.executionContext.comTaskExecutionFailureDueToProblemsOfComTasks(this, preparedComTaskExecution.getComTaskExecution());
            }
            return noProblems;
        } catch (Throwable t) {
            this.failure(preparedComTaskExecution.getComTaskExecution(), t);
            successIndicator = Failure;
            throw t;
        } finally {
            this.completeExecutedComTask(preparedComTaskExecution.getComTaskExecution(), successIndicator);
        }
    }

    ComServerDAO getComServerDAO() {
        return comServerDAO;
    }

    ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    private boolean checkIfWeCanPerformOtherComTaskExecutionsBasedOnConnectionRelatedExceptions(ComServerRuntimeException e) {
        return CommunicationException.class.isAssignableFrom(e.getClass());
    }

    private boolean checkWhetherAllNextComTasksShouldNotBeExecutedBasedOnBasicCheckFailure(ComServerRuntimeException e) {
        return e instanceof TimeDifferenceExceededException || e instanceof SerialNumberMismatchException;
    }

    /**
     * Completes the execution of the {@link ComTaskExecution}.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param successIndicator The SuccessIndicator
     */
    private void completeExecutedComTask(ComTaskExecution comTaskExecution, ComTaskExecutionSession.SuccessIndicator successIndicator) {
        this.getExecutionContext().comTaskExecutionCompleted(this, comTaskExecution, successIndicator);
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
        this.getExecutionContext().comTaskExecutionFailure(this, comTaskExecution, t);
    }

    private CommandRoot.ServiceProvider getComCommandServiceProvider() {
        return new ComCommandServiceProvider();
    }

    protected ComSession.SuccessIndicator getFailureIndicatorFor(RescheduleBehavior.RescheduleReason rescheduleReason) {
        if (RescheduleBehavior.RescheduleReason.CONNECTION_BROKEN.equals(rescheduleReason)) {
            return Broken;
        }
        return ComSession.SuccessIndicator.SetupError;
    }

    PreparedComTaskExecution getPreparedComTaskExecution(ComTaskPreparationContext comTaskPreparationContext, ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        final List<? extends ProtocolTask> protocolTasks = generateProtocolTaskList(comTaskExecution);
        comTaskPreparationContext.getCommandCreator().
                createCommands(
                        comTaskPreparationContext.getRoot(),
                        getProtocolDialectTypedProperties(comTaskExecution),
                        this.preparationContext.getComChannelPlaceHolder(),
                        comTaskPreparationContext.getOfflineDevice(),
                        protocolTasks,
                        deviceProtocolSecurityPropertySet,
                        connectionSteps, comTaskExecution, this.serviceProvider.issueService());
        return new PreparedComTaskExecution(comTaskExecution, comTaskPreparationContext.getRoot(), comTaskPreparationContext.getDeviceProtocol());
    }

    /**
     * We will make sure that the ProtocolTask 'BasicCheck' is the first task of the ComTasks, if it's present.
     * Each <i>set</i> of ProtocolTasks of a ComTask will be preceded by a CreateComTaskSession command.
     */
    private List<ProtocolTask> generateProtocolTaskList(ComTaskExecution comTaskExecution) {
        return comTaskExecution
                .getComTasks()
                .stream()
                .flatMap(comTask -> generateProtocolTaskList(comTask, comTaskExecution))
                .collect(Collectors.toList());
    }

    private Stream<ProtocolTask> generateProtocolTaskList(ComTask comTask, ComTaskExecution comTaskExecution) {
        List<ProtocolTask> protocolTasks = new ArrayList<>(comTask.getProtocolTasks()); // Copies the unmodifiable list
        Collections.sort(protocolTasks, BasicCheckTasks.FIRST);
        protocolTasks.add(0, new CreateComTaskExecutionSessionTask(comTask, comTaskExecution));
        return protocolTasks.stream();
    }

    static TypedProperties getProtocolDialectTypedProperties(ComTaskExecution comTaskExecution) {
        if (isItAScheduledComTaskExecution(comTaskExecution)) {
            ScheduledComTaskExecution scheduledComTaskExecution = (ScheduledComTaskExecution) comTaskExecution;
            return getProtocolDialectTypedProperties(scheduledComTaskExecution);
        } else if (isItAManuallyScheduledComTaskExecution(comTaskExecution)) {
            ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = (ManuallyScheduledComTaskExecution) comTaskExecution;
            return getProtocolDialectTypedProperties(comTaskExecution.getDevice(), manuallyScheduledComTaskExecution.getProtocolDialectConfigurationProperties());
        } else if(isItAFirmwareComTaskExecution(comTaskExecution)){
            FirmwareComTaskExecution firmwareComTaskExecution = (FirmwareComTaskExecution) comTaskExecution;
            return getProtocolDialectTypedProperties(comTaskExecution.getDevice(), firmwareComTaskExecution.getProtocolDialectConfigurationProperties());
        } else {
            return TypedProperties.empty();
        }
    }

    private static boolean isItAFirmwareComTaskExecution(ComTaskExecution comTaskExecution) {
        return comTaskExecution instanceof FirmwareComTaskExecution;
    }

    private static boolean isItAManuallyScheduledComTaskExecution(ComTaskExecution comTaskExecution) {
        return comTaskExecution instanceof ManuallyScheduledComTaskExecution;
    }

    private static boolean isItAScheduledComTaskExecution(ComTaskExecution comTaskExecution) {
        return comTaskExecution instanceof ScheduledComTaskExecution;
    }

    static TypedProperties getProtocolDialectTypedProperties(ScheduledComTaskExecution comTaskExecution) {
        Optional<ProtocolDialectConfigurationProperties> protocolDialectConfigurationProperties = getProtocolDialectConfigurationProperties(comTaskExecution);
        if (protocolDialectConfigurationProperties.isPresent()) {
            Device device = comTaskExecution.getDevice();
            return getProtocolDialectTypedProperties(device, protocolDialectConfigurationProperties.get());
        } else {
            return TypedProperties.empty();
        }
    }

    private static TypedProperties getProtocolDialectTypedProperties(Device device, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        Optional<ProtocolDialectProperties> protocolDialectPropertiesWithName = device.getProtocolDialectProperties(protocolDialectConfigurationProperties.getDeviceProtocolDialectName());
        if (protocolDialectPropertiesWithName.isPresent()) {
            return protocolDialectPropertiesWithName.get().getTypedProperties();
        }
        else {
            return TypedProperties.inheritingFrom(protocolDialectConfigurationProperties.getTypedProperties());
        }
    }

    private static Optional<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationProperties(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.getComTasks().isEmpty()) {
            return Optional.empty();
        } else {
            for (ComTask comTask : comTaskExecution.getComTasks()) {
                Optional<ProtocolDialectConfigurationProperties> properties = getProtocolDialectConfigurationProperties(comTaskExecution.getDevice(), comTask);
                if (properties.isPresent()) {
                    return properties;  // Got out now that we have got one, else continue with the next ComTask
                }
            }
            // Bugger: none of the ComTask were enabled with ProtocolDialectConfigurationProperties
            return Optional.empty();
        }
    }

    private static Optional<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationProperties(Device device, ComTask comTask) {
        for (ComTaskEnablement comTaskEnablement : device.getDeviceConfiguration().getComTaskEnablements()) {
            if (comTaskEnablement.getComTask().getId() == comTask.getId()) {
                return Optional.of(comTaskEnablement.getProtocolDialectConfigurationProperties());
            }
        }
        return Optional.empty();
    }

    private RescheduleBehavior getRescheduleBehavior(ComServerDAO comServerDAO) {
        if (!InboundConnectionTask.class.isAssignableFrom(getConnectionTask().getClass())) {
            ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) getConnectionTask();
            if (scheduledConnectionTask.getConnectionStrategy().equals(ConnectionStrategy.MINIMIZE_CONNECTIONS)) {
                return new RescheduleBehaviorForMinimizeConnections(
                        comServerDAO,
                        this.getSuccessfulComTaskExecutions(),
                        this.getFailedComTaskExecutions(),
                        this.getNotExecutedComTaskExecutions(),
                        getConnectionTask());
            } else {
                return new RescheduleBehaviorForAsap(
                        comServerDAO,
                        this.getSuccessfulComTaskExecutions(),
                        this.getFailedComTaskExecutions(),
                        this.getNotExecutedComTaskExecutions(),
                        getConnectionTask(),
                        getExecutionContext());
            }
        } else {
            return new RescheduleBehaviorForInbound(
                    comServerDAO,
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
        this.getExecutionContext().prepareStart(this, comTaskExecution);
        this.getComServerDAO().executionStarted(comTaskExecution, this.getComPort(), true);
        this.getExecutionContext().executionStarted(comTaskExecution);
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
            this.comChannelPlaceHolder.setComPortRelatedComChannel(comChannel);
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
        public Clock clock() {
            return JobExecution.this.serviceProvider.clock();
        }

        @Override
        public LogBookService logBookService() {
            return JobExecution.this.serviceProvider.logBookService();
        }

        @Override
        public DeviceService deviceService() {
            return JobExecution.this.serviceProvider.deviceService();
        }

        @Override
        public IssueService issueService() {
            return JobExecution.this.serviceProvider.issueService();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return JobExecution.this.serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public TransactionService transactionService() {
            return JobExecution.this.serviceProvider.transactionService();
        }

        @Override
        public IdentificationService identificationService() {
            return JobExecution.this.serviceProvider.identificationService();
        }

        @Override
        public MeteringService meteringService() {
            return JobExecution.this.serviceProvider.meteringService();
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
            offlineDevice = new OfflineDeviceImpl(deviceOrganizedComTaskExecution.getDevice(), offlineDeviceForComTaskGroup, new OfflineDeviceServiceProvider());
        }
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

    private class PrepareAllTransaction implements Transaction<List<PreparedComTaskExecution>> {

        private final List<? extends ComTaskExecution> comTaskExecutions;

        @Override
        public List<PreparedComTaskExecution> perform() {
            List<PreparedComTaskExecution> result = new ArrayList<>();
            ComTaskExecutionOrganizer organizer = new ComTaskExecutionOrganizer(serviceProvider.topologyService());
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
            ComTaskExecutionOrganizer organizer = new ComTaskExecutionOrganizer(serviceProvider.topologyService());
            final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = organizer.defineComTaskExecutionOrders(Arrays.asList(comTaskExecution));
            final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
            if (deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().size() == 1) {
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps comTaskWithSecurityAndConnectionSteps = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(0);
                ComTaskPreparationContext comTaskPreparationContext = new ComTaskPreparationContext(deviceOrganizedComTaskExecution).invoke();
                return getPreparedComTaskExecution(
                        comTaskPreparationContext,
                        comTaskWithSecurityAndConnectionSteps.getComTaskExecution(),
                        comTaskWithSecurityAndConnectionSteps.getComTaskExecutionConnectionSteps(),
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