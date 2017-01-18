package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.exceptions.DeviceCommandException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.InboundDataProcessorDeviceCommandFactory;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.NoopCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLogBookCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedMessageListCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedRegisterCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedTopologyCommandImpl;
import com.energyict.mdc.engine.impl.core.inbound.ComPortDiscoveryLogger;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.TopologyTask;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageAcknowledgement;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An {@link InboundJobExecutionDataProcessor} is responsible for
 * processing collected data which was received during inbound communication.
 * A logical <i>connect</i> can be skipped as the
 * {@link ComChannel ComChannl}
 * will already be created by the ComPortListener
 * <p>
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 3:38 PM
 */
public class InboundJobExecutionDataProcessor extends InboundJobExecutionGroup {

    private static final byte REGISTER_DATA = 0x01;
    private static final byte LOAD_PROFILE_DATA = 0x02;
    private static final byte LOGBOOK_DATA = 0x04;
    private static final byte MESSAGE_DATA = 0x08;
    private static final byte TOPOLOGY_DATA = 0x10;

    private final InboundDeviceProtocol inboundDeviceProtocol;
    private final OfflineDevice offlineDevice;
    private final InboundCommunicationHandler inboundCommunicationHandler;
    private final ComPortDiscoveryLogger comPortDiscoveryLogger;
    private final ServiceProvider serviceProvider;
    protected DeviceProtocol deviceProtocol;
    private Logger logger = Logger.getLogger(InboundJobExecutionDataProcessor.class.getName());

    private boolean notAllCollectedDataIsProcessed;
    private boolean executePendingTaskOnInboundConnection = false;
    private boolean receivedRegisterData = false;
    private boolean receivedLoadProfileData = false;
    private boolean receivedLogBookData = false;
    private boolean receivedMessageData = false;
    private boolean receivedTopologyData = false;

    private Map<ComTaskExecution, List<ProtocolTask>> protocolTasksAlreadyExecutedForComTask = new HashMap<>();


    public InboundJobExecutionDataProcessor(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundDiscoveryContextImpl inboundDiscoveryContext, InboundDeviceProtocol inboundDeviceProtocol, OfflineDevice offlineDevice, ServiceProvider serviceProvider, InboundCommunicationHandler inboundCommunicationHandler, ComPortDiscoveryLogger logger, boolean executePendingTaskOnInboundConnection) {
        super(comPort, comServerDAO, deviceCommandExecutor, inboundDiscoveryContext, serviceProvider);
        this.inboundDeviceProtocol = inboundDeviceProtocol;
        this.offlineDevice = offlineDevice;
        this.serviceProvider = serviceProvider;
        this.inboundCommunicationHandler = inboundCommunicationHandler;
        this.comPortDiscoveryLogger = logger;
        this.executePendingTaskOnInboundConnection = executePendingTaskOnInboundConnection;
    }

    @Override
    protected void setExecutionContext(ExecutionContext executionContext) {
        executionContext.setComSessionBuilder(getInboundDiscoveryContext().getComSessionBuilder());
        super.setExecutionContext(executionContext);
    }

    /**
     * Wait for the storing to finish (using a Future), provide a proper response to the meter once it's done.
     * This is different from standard behavior: normally, the execution of the tasks and the storing of the results is not linked.
     */
    @Override
    public Future<Boolean> doExecuteStoreCommand() {
        Future<Boolean> future = super.doExecuteStoreCommand();
        try {
            future.get();        //Blocking call until the device command is fully executed (= collected data is stored)
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw DeviceCommandException.errorDuringFutureGetCall(e, MessageSeeds.INBOUND_DATA_PROCESSOR_ERROR);
        }
        return future;
    }

    @Override
    protected ExecutionContext newExecutionContext(ConnectionTask connectionTask, ComPort comPort, boolean logConnectionProperties) {
        ExecutionContext executionContext = super.newExecutionContext(connectionTask, comPort, logConnectionProperties);
        executionContext.setDeviceCommandFactory(new InboundDataProcessorDeviceCommandFactory(executionContext, inboundCommunicationHandler, inboundDeviceProtocol));
        return executionContext;
    }

    @Override
    protected CommandRoot prepareAll(List<ComTaskExecution> comTaskExecutions) {
        CommandRoot root = new CommandRootImpl(getExecutionContext(), new CommandRootServiceProvider(), true);    //True: expose any exceptions that occur while attempting to store the collected data
        GroupedDeviceCommand groupedDeviceCommand = null;
        try {
            byte alreadyProcessedDataTypes = 0;
            Set<CollectedData> processedCollectedData = new HashSet<>();
            groupedDeviceCommand = root.getOrCreateGroupedDeviceCommand(offlineDevice, getDeviceProtocol(), null);
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                List<ServerCollectedData> data = receivedCollectedDataFor(comTaskExecution);
                processedCollectedData.addAll(data);

                if (!data.isEmpty()) {
                    postProcess(data);
                    ProtocolTask registersTask = getRegistersTask(comTaskExecution);
                    ProtocolTask loadProfilesTask = getLoadProfilesTask(comTaskExecution);
                    ProtocolTask logBooksTask = getLogBooksTask(comTaskExecution);
                    ProtocolTask messageTask = getMessageTask(comTaskExecution);
                    ProtocolTask topologyTask = getTopologyTask(comTaskExecution);
                    List<ProtocolTask> protocolTasksToExecute = new ArrayList<>();
                    for (ComTask comTask : comTaskExecution.getComTasks()) {
                        protocolTasksToExecute.addAll(comTask.getProtocolTasks());
                    }

                    if (registersTask != null && receivedRegisterData) {
                        if (!alreadyProcessedDataOfType(alreadyProcessedDataTypes, REGISTER_DATA)) {
                            InboundCollectedRegisterCommandImpl inboundCollectedRegisterListCommand = new InboundCollectedRegisterCommandImpl(groupedDeviceCommand, (RegistersTask) registersTask, comTaskExecution, data);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedRegisterListCommand);
                            alreadyProcessedDataTypes |= REGISTER_DATA;
                        } else {
                            addNewNoopCommand(groupedDeviceCommand, comTaskExecution);
                        }
                        protocolTasksToExecute.remove(registersTask);
                    }
                    if (loadProfilesTask != null && receivedLoadProfileData) {
                        if (!alreadyProcessedDataOfType(alreadyProcessedDataTypes, LOAD_PROFILE_DATA)) {
                            InboundCollectedLoadProfileCommandImpl inboundCollectedLoadProfileReadCommand = new InboundCollectedLoadProfileCommandImpl(groupedDeviceCommand, (LoadProfilesTask) loadProfilesTask, comTaskExecution, data);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedLoadProfileReadCommand);
                            alreadyProcessedDataTypes |= LOAD_PROFILE_DATA;
                        } else {
                            addNewNoopCommand(groupedDeviceCommand, comTaskExecution);
                        }
                        protocolTasksToExecute.remove(loadProfilesTask);
                    }
                    if (logBooksTask != null && receivedLogBookData) {
                        if (!alreadyProcessedDataOfType(alreadyProcessedDataTypes, LOGBOOK_DATA)) {
                            InboundCollectedLogBookCommandImpl inboundCollectedLogBookReadCommand = new InboundCollectedLogBookCommandImpl(groupedDeviceCommand, (LogBooksTask) logBooksTask, comTaskExecution, data);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedLogBookReadCommand);
                            alreadyProcessedDataTypes |= LOGBOOK_DATA;
                        } else {
                            addNewNoopCommand(groupedDeviceCommand, comTaskExecution);
                        }
                        protocolTasksToExecute.remove(logBooksTask);
                    }
                    if (messageTask != null && receivedMessageData) {
                        if (!alreadyProcessedDataOfType(alreadyProcessedDataTypes, MESSAGE_DATA)) {
                            InboundCollectedMessageListCommandImpl inboundCollectedMessageListCommand = new InboundCollectedMessageListCommandImpl(groupedDeviceCommand, (MessagesTask) messageTask, comTaskExecution, data);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedMessageListCommand);
                            alreadyProcessedDataTypes |= MESSAGE_DATA;
                        } else {
                            addNewNoopCommand(groupedDeviceCommand, comTaskExecution);
                        }
                        protocolTasksToExecute.remove(messageTask);
                    }
                    if (topologyTask != null && receivedTopologyData) {
                        if (!alreadyProcessedDataOfType(alreadyProcessedDataTypes, TOPOLOGY_DATA)) {
                            InboundCollectedTopologyCommandImpl inboundCollectedTopologyCommand = new InboundCollectedTopologyCommandImpl(groupedDeviceCommand, (TopologyTask) topologyTask, comTaskExecution, data);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedTopologyCommand);
                            alreadyProcessedDataTypes |= TOPOLOGY_DATA;
                        } else {
                            addNewNoopCommand(groupedDeviceCommand, comTaskExecution);
                        }
                        protocolTasksToExecute.remove(topologyTask);
                    }

                    protocolTasksAlreadyExecutedForComTask.put(comTaskExecution, protocolTasksToExecute);
                }
            }

            for (CollectedData collectedData : inboundDeviceProtocol.getCollectedData(offlineDevice)) {
                if (!dataWasProcessed(processedCollectedData, collectedData)) {
                    getInboundDiscoveryContext().markNotAllCollectedDataWasProcessed();
                    logDroppedDataOnComPortDiscoveryLogger(collectedData.getClass().getSimpleName());
                    notAllCollectedDataIsProcessed = true;
                }
            }

        } catch (Throwable e) {
            root.generalSetupErrorOccurred(e, comTaskExecutions);
            root.removeAllGroupedDeviceCommands();
        }

        if (executePendingTaskOnInboundConnection) {
            //TODO port COMMUNICATION-1587
            //execute remaining pending tasks on same inbound connection
            //return super.prepareAll(comTaskExecutions, root, groupedDeviceCommand, false);
            return root;
        } else {
            return root;
        }
    }

    @Override
    protected void prepareComTaskExecution(ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet, GroupedDeviceCommand groupedDeviceCommand, CommandCreator commandCreator) {
        List<ProtocolTask> allProtocolTasks = new ArrayList<>();
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            allProtocolTasks.addAll(comTask.getProtocolTasks());
        }

        final List<ProtocolTask> protocolTasks =
                protocolTasksAlreadyExecutedForComTask.get(comTaskExecution) != null ?
                        protocolTasksAlreadyExecutedForComTask.get(comTaskExecution) :
                        allProtocolTasks;

        if (protocolTasks.size() > 0) {
            Collections.sort(protocolTasks, BasicCheckTasks.FIRST);

            commandCreator.createCommands(
                    groupedDeviceCommand,
                    getProtocolDialectTypedProperties(comTaskExecution),
                    super.preparationContext.getComChannelPlaceHolder(),
                    protocolTasks,
                    deviceProtocolSecurityPropertySet,
                    connectionSteps,
                    comTaskExecution,
                    getServiceProvider().issueService());
        }
    }

    private boolean dataWasProcessed(Set<CollectedData> processedCollectedDatas, CollectedData collectedData) {
        for (CollectedData processedCollectedData : processedCollectedDatas) {
            if (processedCollectedData.getClass().equals(collectedData.getClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if data of the given type is already processed.
     *
     * @param alreadyProcessedDataTypes The bitstring containing the already processed data types
     * @param dataType                  The byte indicating the dataType
     * @return true if the given data type is already processed
     * false if the given data type was not yet processed
     */
    private boolean alreadyProcessedDataOfType(byte alreadyProcessedDataTypes, byte dataType) {
        return (alreadyProcessedDataTypes & dataType) == dataType;
    }

    private void postProcess(List<ServerCollectedData> data) {
        for (ServerCollectedData collectedData : data) {
            collectedData.postProcess(getConnectionTask());
        }
    }

    private void addNewInboundComCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, ComCommand comCommand) {
        groupedDeviceCommand.addCommand(comCommand, comTaskExecution);
        getInboundDiscoveryContext().getComSessionBuilder().incrementNotExecutedTasks(1);
    }

    private void addNewNoopCommand(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        NoopCommandImpl noopCommand = new NoopCommandImpl(groupedDeviceCommand);
        addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, noopCommand);
    }

    private void logDroppedDataOnComPortDiscoveryLogger(String dataType) {
        comPortDiscoveryLogger.collectedDataWasFiltered(
                dataType,
                this.offlineDevice.getDeviceIdentifier(),
                getComPort());
    }

    private ProtocolTask getMessageTask(ComTaskExecution comTaskExecution) {
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                if (ComCommandTypes.MESSAGES_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                    return protocolTask;
                }
            }
        }
        return null;
    }

    private ProtocolTask getLogBooksTask(ComTaskExecution comTaskExecution) {
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                if (ComCommandTypes.LOGBOOKS_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                    return protocolTask;
                }
            }
        }
        return null;
    }


    private ProtocolTask getLoadProfilesTask(ComTaskExecution comTaskExecution) {
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                if (ComCommandTypes.LOAD_PROFILE_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                    return protocolTask;
                }
            }
        }
        return null;
    }

    private ProtocolTask getRegistersTask(ComTaskExecution comTaskExecution) {
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                if (ComCommandTypes.REGISTERS_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                    return protocolTask;
                }
            }
        }
        return null;
    }

    private ProtocolTask getTopologyTask(ComTaskExecution comTaskExecution) {
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                if (ComCommandTypes.TOPOLOGY_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                    return protocolTask;
                }
            }
        }
        return null;
    }

    private ProtocolTask getClockTask(ComTaskExecution comTaskExecution) {
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                if (ComCommandTypes.CLOCK_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                    return protocolTask;
                }
            }
        }
        return null;
    }

    private ProtocolTask getBasicCheckTask(ComTaskExecution comTaskExecution) {
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                if (ComCommandTypes.BASIC_CHECK_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                    return protocolTask;
                }
            }
        }
        return null;
    }

    protected DeviceProtocol getDeviceProtocol() {
        if (this.deviceProtocol == null) {
            DeviceProtocolPluggableClass protocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
            this.deviceProtocol = protocolPluggableClass.getDeviceProtocol();
        }
        return this.deviceProtocol;
    }

    private List<ServerCollectedData> receivedCollectedDataFor(ComTaskExecution comTaskExecution) {
        List<ServerCollectedData> collectedDatas = new ArrayList<>();
        resetReceivedDataFlags();
        for (CollectedData collectedData : inboundDeviceProtocol.getCollectedData(offlineDevice)) {
            if (collectedData.isConfiguredIn(comTaskExecution)) {
                ServerCollectedData dataItem = (ServerCollectedData) collectedData;
                collectedDatas.add(dataItem);
                updateReceivedDataFlags(dataItem);
            }
        }
        return collectedDatas;
    }

    private void resetReceivedDataFlags() {
        receivedRegisterData = false;
        receivedLoadProfileData = false;
        receivedLogBookData = false;
        receivedMessageData = false;
        receivedTopologyData = false;
    }

    private void updateReceivedDataFlags(ServerCollectedData dataItem) {
        if (!receivedRegisterData) {
            receivedRegisterData = receivedRegisterDataOnInbound(dataItem);
        }
        if (!receivedLoadProfileData) {
            receivedLoadProfileData = receivedLoadProfileDataOnInbound(dataItem);
        }
        if (!receivedLogBookData) {
            receivedLogBookData = receivedLogBookDataOnInbound(dataItem);
        }
        if (!receivedMessageData) {
            receivedMessageData = receivedMessageDataOnInbound(dataItem);
        }
        if (!receivedTopologyData) {
            receivedTopologyData = receivedTopologyDataOnInbound(dataItem);
        }
    }


    public boolean receivedRegisterDataOnInbound(ServerCollectedData dataItem) {
        return dataItem instanceof CollectedRegister
                | dataItem instanceof CollectedRegisterList
                | dataItem instanceof CollectedDeviceInfo;
    }

    public boolean receivedLoadProfileDataOnInbound(ServerCollectedData dataItem) {
        return dataItem instanceof CollectedLoadProfile;
    }

    public boolean receivedLogBookDataOnInbound(ServerCollectedData dataItem) {
        return dataItem instanceof CollectedLogBook;
    }

    public boolean receivedMessageDataOnInbound(ServerCollectedData dataItem) {
        return dataItem instanceof CollectedMessage
                | dataItem instanceof CollectedMessageList
                | dataItem instanceof CollectedMessageAcknowledgement;
    }

    public boolean receivedTopologyDataOnInbound(ServerCollectedData dataItem) {
        return dataItem instanceof CollectedTopology;
    }

    private class CommandRootServiceProvider implements CommandRoot.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public IssueService issueService() {
            return serviceProvider.issueService();
        }

        @Override
        public Thesaurus thesaurus() {
            return serviceProvider.thesaurus();
        }

        @Override
        public DeviceService deviceService() {
            return serviceProvider.deviceService();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public TransactionService transactionService() {
            return serviceProvider.transactionService();
        }

        @Override
        public IdentificationService identificationService() {
            return serviceProvider.identificationService();
        }

        @Override
        public MeteringService meteringService() {
            return serviceProvider.meteringService();
        }
    }
}