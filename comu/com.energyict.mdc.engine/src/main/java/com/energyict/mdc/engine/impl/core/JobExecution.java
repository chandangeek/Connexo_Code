/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.ProtocolDialectProperties;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.BasicCheckTask;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.OfflineDeviceForComTaskGroup;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.PublishConnectionCompletionEvent;
import com.energyict.mdc.engine.impl.commands.store.RescheduleFailedExecution;
import com.energyict.mdc.engine.impl.commands.store.RescheduleSuccessfulExecution;
import com.energyict.mdc.engine.impl.commands.store.core.BasicComCommandBehavior;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.exceptions.ConnectionException;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.mdc.upl.DeviceProtocolDialect.Property.DEVICE_PROTOCOL_DIALECT;

/**
 * Provides code reuse for in- and outbound {@link ComPort ComPorts }
 * which perform one or more ComTasks.
 * It will be useful to group the AOP logging as well.
 * <p>
 *
 * Date: 25/10/12
 * Time: 16:27
 */
public abstract class JobExecution implements ScheduledJob {

    private static final Logger LOGGER = Logger.getLogger(JobExecution.class.getName());

    private final ComPort comPort;
    private final ComServerDAO comServerDAO;
    private final DeviceCommandExecutor deviceCommandExecutor;
    private final ServiceProvider serviceProvider;
    // The root command that will start all subcommands
    protected CommandRoot commandRoot;
    protected PreparationContext preparationContext;
    private DeviceCommandExecutionToken token;
    private ExecutionContext executionContext;

    public JobExecution(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
        this.preparationContext = new PreparationContext();
    }

    protected static TypedProperties getProtocolDialectTypedProperties(ConnectionTask connectionTask) {
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = connectionTask.getProtocolDialectConfigurationProperties();
        Device device = connectionTask.getDevice();
        Optional<ProtocolDialectProperties> protocolDialectPropertiesWithName = device.getProtocolDialectProperties(protocolDialectConfigurationProperties.getDeviceProtocolDialectName());
        TypedProperties result;
        if (protocolDialectPropertiesWithName.isPresent()) {
            result = protocolDialectPropertiesWithName.get().getTypedProperties();
        } else {
            result = TypedProperties.inheritingFrom(protocolDialectConfigurationProperties != null ?
                    protocolDialectConfigurationProperties.getTypedProperties()
                    : TypedProperties.empty());
        }
        addDefaultValuesIfNecessary(protocolDialectConfigurationProperties, result);
        addProtocolDialectNameAsProperty(protocolDialectConfigurationProperties, result);
        return result;
    }

    /**
     * For all properties who are not yet specified - but for which a default value exist - the default value will be added.
     */
    private static void addDefaultValuesIfNecessary(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, TypedProperties result) {
        if (protocolDialectConfigurationProperties != null) {
            for (PropertySpec propertySpec : protocolDialectConfigurationProperties.getUPLPropertySpecs()) {
                if (!result.hasValueFor(propertySpec.getName()) && propertySpec.getPossibleValues() != null) {
                    result.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getDefault());
                }
            }
        }
    }

    private static void addProtocolDialectNameAsProperty(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, TypedProperties result) {
        if (protocolDialectConfigurationProperties != null) { // Should always be the case, but just to be sure
            result.setProperty(DEVICE_PROTOCOL_DIALECT.getName(), protocolDialectConfigurationProperties.getDeviceProtocolDialectName());
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

    /**
     * Provide a list of {@link ComTaskExecution comTaskExecutions} which will be executed during this session
     *
     * @return the list of ComTaskExecutions
     */
    public abstract List<ComTaskExecution> getComTaskExecutions();

    @Override
    public boolean containsOneOf(List<ComTaskExecution> comTaskExecutions) {
        Set<ComTaskExecution> comTaskExecutionSet = new HashSet<>(comTaskExecutions);
        for (ComTaskExecution comTaskExecution : this.getComTaskExecutions()) {
            if (comTaskExecutionSet.contains(comTaskExecution)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicate whether a connection has been set-up
     *
     * @return true if the connection is up, false otherwise
     */
    public abstract boolean isConnected();

    protected List<ComTaskExecution> getNotExecutedComTaskExecutions() {
        List<ComTaskExecution> notExecuted = new ArrayList<>();
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                if (comTaskExecutionComCommand.getExecutionState().equals(BasicComCommandBehavior.ExecutionState.NOT_EXECUTED)) {
                    notExecuted.add(comTaskExecutionComCommand.getComTaskExecution());
                }
            }
        }
        return notExecuted;
    }

    protected List<ComTaskExecution> getFailedComTaskExecutions() {
        List<ComTaskExecution> failed = new ArrayList<>();
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                if (comTaskExecutionComCommand.getExecutionState().equals(BasicComCommandBehavior.ExecutionState.FAILED)) {
                    failed.add(comTaskExecutionComCommand.getComTaskExecution());
                }
            }
        }
        return failed;
    }

    protected List<ComTaskExecution> getSuccessfulComTaskExecutions() {
        List<ComTaskExecution> success = new ArrayList<>();
        for (GroupedDeviceCommand groupedDeviceCommand : commandRoot) {
            for (ComTaskExecutionComCommandImpl comTaskExecutionComCommand : groupedDeviceCommand) {
                if (comTaskExecutionComCommand.getExecutionState().equals(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED)) {
                    success.add(comTaskExecutionComCommand.getComTaskExecution());
                }
            }
        }
        return success;
    }

    public abstract ConnectionTask getConnectionTask();

    public ComPort getComPort() {
        return comPort;
    }

    public ComServerDAO getComServerDAO() {
        return comServerDAO;
    }

    protected void prepareComTaskExecution(ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet, GroupedDeviceCommand groupedDeviceCommand, CommandCreator commandCreator) {
        final List<ProtocolTask> protocolTasks = generateProtocolTaskList(comTaskExecution);
        commandCreator.createCommands(
                groupedDeviceCommand,
                getProtocolDialectTypedProperties(getConnectionTask()),
                this.preparationContext.getComChannelPlaceHolder(),
                protocolTasks,
                deviceProtocolSecurityPropertySet,
                connectionSteps,
                comTaskExecution,
                this.serviceProvider.issueService()
        );
    }

    /**
     * We will make sure that the ProtocolTask 'BasicCheck' is the first task of the ComTasks, if it's present.
     * Each <i>set</i> of ProtocolTasks of a ComTask will be preceded by a CreateComTaskSession command.
     */
    private List<ProtocolTask> generateProtocolTaskList(ComTaskExecution comTaskExecution) {
        return generateProtocolTaskList(comTaskExecution.getComTask())
                .collect(Collectors.toList());
    }

    private Stream<ProtocolTask> generateProtocolTaskList(ComTask comTask) {
        List<ProtocolTask> protocolTasks = new ArrayList<>(comTask.getProtocolTasks()); // Copies the unmodifiable list
        Collections.sort(protocolTasks, BasicCheckTasks.FIRST);
        return protocolTasks.stream();
    }

    protected CommandRoot prepareAll(final List<ComTaskExecution> comTaskExecutions) {
        getExecutionContext().getComSessionBuilder().setNotExecutedTasks(comTaskExecutions.size());
        return this.serviceProvider.transactionService().execute(new PrepareAllTransaction(this, comTaskExecutions));
    }

    protected CommandRoot prepareAll(CommandRoot commandRoot, final List<ComTaskExecution> comTaskExecutions) {
        getExecutionContext().getComSessionBuilder().setNotExecutedTasks(comTaskExecutions.size());
        return this.serviceProvider.transactionService().execute(new PrepareAllTransaction(this, commandRoot, comTaskExecutions));
    }

    protected CommandRoot prepareAll(final List<ComTaskExecution> comTaskExecutions, boolean checkSecurity) {
        getExecutionContext().getComSessionBuilder().setNotExecutedTasks(comTaskExecutions.size());
        return this.serviceProvider.transactionService().execute(new PrepareAllTransaction(this, comTaskExecutions, checkSecurity));
    }

    protected CommandRoot prepareAll(CommandRoot commandRoot, final List<ComTaskExecution> comTaskExecutions, boolean checkSecurity) {
        getExecutionContext().getComSessionBuilder().setNotExecutedTasks(comTaskExecutions.size());
        return this.serviceProvider.transactionService().execute(new PrepareAllTransaction(this, commandRoot, comTaskExecutions, checkSecurity));
    }

    public void connected(ComPortRelatedComChannel comChannel) {
        this.preparationContext.setComChannel(comChannel);
    }

    protected DeviceCommandExecutor getDeviceCommandExecutor() {
        return deviceCommandExecutor;
    }

    public DeviceCommandExecutionToken getToken() {
        return token;
    }

    @Override
    public void setToken(DeviceCommandExecutionToken deviceCommandExecutionToken) {
        this.token = deviceCommandExecutionToken;
    }

    public void releaseToken() {
        this.getDeviceCommandExecutor().free(this.getToken());
    }

    public void releaseTokenSilently() {
        this.getDeviceCommandExecutor().freeSilently(this.getToken());
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    protected void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public String getThreadName() {
        return Thread.currentThread().getName();
    }

    public void closeConnection() {
        if (getExecutionContext() != null) {
            getExecutionContext().close();
        }
    }

    protected void completeSuccessfulComSession(ComSession.SuccessIndicator reason) {
        addCompletionEvent();
        getExecutionContext().completeSuccessful(reason);

        getExecutionContext().getStoreCommand().add(new RescheduleSuccessfulExecution(this, getExecutionContext().getDeviceCommandServiceProvider()));
        doExecuteStoreCommand();
    }

    protected void completeFailedComSession(ComSession.SuccessIndicator reason) {
        ExecutionContext executionContext = this.getExecutionContext();
        if (executionContext != null) {
            addCompletionEvent();
            getExecutionContext().completeFailure(reason);
        } else {
            executionContext = new ExecutionContext(this, getConnectionTask(), getComPort(), serviceProvider);
            setExecutionContext(executionContext);
        }
        getExecutionContext().getStoreCommand().add(new RescheduleFailedExecution(this, executionContext.getDeviceCommandServiceProvider()));
        doExecuteStoreCommand();
    }

    protected void completeOutsideComWindow() {
        getExecutionContext().completeOutsideComWindow();
        doExecuteStoreCommand();
    }

    private void addCompletionEvent() {
        ExecutionContext executionContext = this.getExecutionContext();
        if (executionContext != null && serviceProvider.engineService().isOnlineMode()) {
            if (!(executionContext.connectionFailed()
                    || onlyNotExecutedComTasks() && getSuccessIndicator().equals(ComSession.SuccessIndicator.Not_Executed))) {
                executionContext.getStoreCommand().add(
                        new PublishConnectionCompletionEvent(
                                getConnectionTask(),
                                getComPort(),
                                getSuccessfulComTaskExecutions(),
                                getFailedComTaskExecutions(),
                                getNotExecutedComTaskExecutions(),
                                executionContext.getDeviceCommandServiceProvider()));
            }
        } else {
            // Failure was in the preparation that creates the ExecutionContext
            LOGGER.log(
                    Level.FINE,
                    "Attempt to publish an event that a ConnectionTask has completed for OfflineEngine or no ExecutionContext!",
                    new Exception("For diagnostic purposes only"));
        }
    }

    private boolean onlyNotExecutedComTasks() {
        return getSuccessfulComTaskExecutions().isEmpty() && getFailedComTaskExecutions().isEmpty() && !getNotExecutedComTaskExecutions().isEmpty();
    }

    /**
     * Store all the collected data and create a ComSession
     */
    protected Future<Boolean> doExecuteStoreCommand() {
        return getDeviceCommandExecutor().execute(getExecutionContext().getStoreCommand(), getToken());
    }

    protected ExecutionContext newExecutionContext(ConnectionTask connectionTask, ComPort comPort) {
        return newExecutionContext(connectionTask, comPort, true);
    }

    protected ExecutionContext newExecutionContext(ConnectionTask connectionTask, ComPort comPort, boolean logConnectionProperties) {
        return new ExecutionContext(this, connectionTask, comPort, logConnectionProperties, getServiceProvider());
    }

    protected CommandRoot initCommandRoot(){
        return new CommandRootImpl(getExecutionContext(), getComCommandServiceProvider());
    }

    private ComSession.SuccessIndicator getSuccessIndicator() {
        ComSession.SuccessIndicator successIndicator = ComSession.SuccessIndicator.Success;
        if (commandRoot.hasConnectionNotExecuted()) {
            successIndicator = ComSession.SuccessIndicator.Not_Executed;
        } else if (commandRoot.hasConnectionSetupError()) {
            successIndicator = ComSession.SuccessIndicator.SetupError;
        } else if (commandRoot.hasConnectionErrorOccurred()) {
            successIndicator = ComSession.SuccessIndicator.Broken;
        } else if (commandRoot.hasGeneralSetupErrorOccurred()) {
            successIndicator = ComSession.SuccessIndicator.Broken;
        } else if (commandRoot.hasConnectionBeenInterrupted()) {
            successIndicator = ComSession.SuccessIndicator.Interrupted;
        }
        return successIndicator;
    }

    @Override
    public void reschedule() {
        ComSession.SuccessIndicator successIndicator = getSuccessIndicator();

        if (successIndicator.equals(ComSession.SuccessIndicator.Success) || successIndicator.equals(ComSession.SuccessIndicator.Not_Executed)) {
            rescheduleSuccess(successIndicator);
        } else {
            rescheduleFailure(successIndicator);
        }
    }

    private void rescheduleFailure(ComSession.SuccessIndicator successIndicator) {
        for (ComTaskExecution failedComTaskExecution : getFailedComTaskExecutions()) {
            getExecutionContext().connectionLogger.reschedulingTask(getThreadName(), failedComTaskExecution.getComTask().getName());
        }

        this.completeFailedComSession(successIndicator);
    }

    private void rescheduleSuccess(ComSession.SuccessIndicator reason) {
        completeSuccessfulComSession(reason);
    }

    public void doReschedule() {
        getRescheduleBehavior().reschedule(commandRoot);
    }

    public void doRescheduleToNextComWindow(Instant startingPoint) {
        getRescheduleBehavior().rescheduleOutsideComWindow(getComTaskExecutions(), startingPoint);
    }

    RescheduleBehavior getRescheduleBehavior() {
        if (!InboundConnectionTask.class.isAssignableFrom(getConnectionTask().getClass())) {
            ScheduledConnectionTask outboundConnectionTask = (ScheduledConnectionTask) getConnectionTask();
            if (outboundConnectionTask.getConnectionStrategy().equals(ConnectionStrategy.MINIMIZE_CONNECTIONS)) {
                return new RescheduleBehaviorForMinimizeConnections(getComServerDAO(), getConnectionTask(), getServiceProvider().clock());
            } else {
                return new RescheduleBehaviorForAsap(getComServerDAO(), getConnectionTask(), getServiceProvider().clock());
            }
        } else {
            return new RescheduleBehaviorForInbound(getComServerDAO(), getConnectionTask(), getServiceProvider().clock());
        }
    }

    ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public CommandRoot.ServiceProvider getComCommandServiceProvider() {
        return new ComCommandServiceProvider();
    }

    public abstract void appendStatisticalInformationToComSession();

    protected enum BasicCheckTasks implements Comparator<ProtocolTask> {
        FIRST;

        @Override
        public int compare(ProtocolTask o1, ProtocolTask o2) {
            if (o1 instanceof BasicCheckTask) {
                return o2 instanceof BasicCheckTask ? 0 : -1;
            }
            return o2 instanceof BasicCheckTask ? 1 : 0;
        }
    }

    public interface ServiceProvider extends ExecutionContext.ServiceProvider {

        DeviceConfigurationService deviceConfigurationService();

        ConnectionTaskService connectionTaskService();

        Thesaurus thesaurus();

        DeviceService deviceService();

        TopologyService topologyService();

        EngineService engineService();

        MdcReadingTypeUtilService mdcReadingTypeUtilService();

        HexService hexService();

        TransactionService transactionService();

        EventService eventService();

        IdentificationService identificationService();

        MeteringService meteringService();

        FirmwareService firmwareService();

        ProtocolPluggableService protocolPluggableService();

        TimeOfUseCampaignService touService();

    }

    /**
     * Provides context information to the process
     * that prepares {@link ComTaskExecution}s for execution.
     */
    protected static class PreparationContext {

        private ComChannelPlaceHolder comChannelPlaceHolder = ComChannelPlaceHolder.empty();

        ComChannelPlaceHolder getComChannelPlaceHolder() {
            return comChannelPlaceHolder;
        }

        public void setComChannel(ComPortRelatedComChannel comChannel) {
            this.comChannelPlaceHolder.setComPortRelatedComChannel(comChannel);
        }
    }

    private class PrepareAllTransaction implements Transaction<CommandRoot> {

        private final List<ComTaskExecution> comTaskExecutions;
        private final JobExecution jobExecution;
        private final boolean checkSecurity;

        private PrepareAllTransaction(JobExecution jobExecution, List<ComTaskExecution> comTaskExecutions) {
            this.jobExecution = jobExecution;
            this.comTaskExecutions = comTaskExecutions;
            this.checkSecurity = true;
        }

        private PrepareAllTransaction(JobExecution jobExecution, List<ComTaskExecution> comTaskExecutions, boolean checkSecurity) {
            this.jobExecution = jobExecution;
            this.comTaskExecutions = comTaskExecutions;
            this.checkSecurity = checkSecurity;
        }

        private PrepareAllTransaction(JobExecution jobExecution, CommandRoot commandRoot, List<ComTaskExecution> comTaskExecutions) {
            this(jobExecution, comTaskExecutions);
            JobExecution.this.commandRoot = commandRoot;
        }

        private PrepareAllTransaction(JobExecution jobExecution, CommandRoot commandRoot, List<ComTaskExecution> comTaskExecutions, boolean checkSecurity) {
            this(jobExecution, comTaskExecutions, checkSecurity);
            JobExecution.this.commandRoot = commandRoot;
        }

        @Override
        public CommandRoot perform() {
            CommandRoot result = commandRoot != null ? commandRoot : initCommandRoot();
            try {
                ComTaskExecutionOrganizer organizer = new ComTaskExecutionOrganizer(serviceProvider.topologyService());

                final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = organizer.defineComTaskExecutionOrders(this.comTaskExecutions);
                for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : deviceOrganizedComTaskExecutions) {

                    final OfflineDeviceForComTaskGroup offlineDeviceForComTaskGroup = new OfflineDeviceForComTaskGroup(deviceOrganizedComTaskExecution.getComTaskExecutions());
                    DeviceIdentifier deviceIdentifier = getComCommandServiceProvider().identificationService().createDeviceIdentifierForAlreadyKnownDevice(deviceOrganizedComTaskExecution.getDevice().getId(), deviceOrganizedComTaskExecution.getDevice().getmRID());
                    OfflineDevice offlineDevice = getComServerDAO().findOfflineDevice(deviceIdentifier, offlineDeviceForComTaskGroup).get();
                    DeviceProtocolPluggableClass deviceProtocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
                    DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
                    CommandCreator commandCreator = CommandFactory.commandCreatorForPluggableClass(deviceProtocolPluggableClass);
                    GroupedDeviceCommand groupedDeviceCommand = null;

                    for (DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps comTaskWithSecurityAndConnectionSteps : deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity()) {

                        final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet();
                        final ComTaskExecution comTaskExecution = comTaskWithSecurityAndConnectionSteps.getComTaskExecution();
                        final ComTaskExecutionConnectionSteps connectionSteps = comTaskWithSecurityAndConnectionSteps.getComTaskExecutionConnectionSteps();

                        if (groupedDeviceCommand == null || (checkSecurity && !isSameSecurityPropertySet(groupedDeviceCommand, deviceProtocolSecurityPropertySet))) {
                            groupedDeviceCommand = result.getOrCreateGroupedDeviceCommand(offlineDevice, deviceProtocol, comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet());
                        }

                        jobExecution.prepareComTaskExecution(
                                comTaskExecution,
                                connectionSteps,
                                deviceProtocolSecurityPropertySet,
                                groupedDeviceCommand,
                                commandCreator);

                    }
                }
            } catch (Throwable e) {
                result.generalSetupErrorOccurred(e, comTaskExecutions);
                result.removeAllGroupedDeviceCommands();
            }
            return result;
        }

        private boolean isSameSecurityPropertySet(GroupedDeviceCommand groupedDeviceCommand, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
            return groupedDeviceCommand.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel() == deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()
                    && groupedDeviceCommand.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel() == deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel();
        }
    }

    protected class ComCommandServiceProvider implements CommandRoot.ServiceProvider {

        @Override
        public Clock clock() {
            return JobExecution.this.serviceProvider.clock();
        }

        @Override
        public Thesaurus thesaurus() {
            return JobExecution.this.serviceProvider.thesaurus();
        }

        @Override
        public NlsService nlsService() {
            return JobExecution.this.serviceProvider.nlsService();
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

        @Override
        public DeviceMessageService deviceMessageService() {
            return JobExecution.this.serviceProvider.deviceMessageService();
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return JobExecution.this.serviceProvider.protocolPluggableService();
        }
    }
}
