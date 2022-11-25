/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.LogBooksTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.common.tasks.TopologyTask;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.exceptions.DeviceCommandException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.InboundDataProcessorDeviceCommandFactory;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.BackFillLoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLogBookCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedMessageListCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedRegisterCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedStatusInformationCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedTopologyCommandImpl;
import com.energyict.mdc.engine.impl.core.inbound.ComPortDiscoveryLogger;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.meterdata.DeviceConnectionProperty;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.LoadProfileType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.LoadProfileReader;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link InboundJobExecutionDataProcessor} is responsible for
 * processing collected data which was received during inbound communication.
 * A logical <i>connect</i> can be skipped as the
 * {@link ComChannel ComChannl}
 * will already be created by the ComPortListener
 * <p>
 * <p>
 * Date: 9/3/13
 * Time: 3:38 PM
 */
public class InboundJobExecutionDataProcessor extends InboundJobExecutionGroup {

    public static final String PROPERTY_BACK_FILL_ON_INBOUND = "BackFillOnInbound";

    private final com.energyict.mdc.upl.InboundDeviceProtocol inboundDeviceProtocol;
    private final OfflineDevice offlineDevice;
    private final InboundCommunicationHandler inboundCommunicationHandler;
    private final ComPortDiscoveryLogger comPortDiscoveryLogger;
    private final ServiceProvider serviceProvider;

    private final Logger logger = Logger.getLogger(InboundJobExecutionDataProcessor.class.getName());
    private final boolean executePendingTaskOnInboundConnection;
    private final Map<ComTaskExecution, List<ProtocolTask>> protocolTasksAlreadyExecutedForComTask = new HashMap<>();

    protected DeviceProtocol deviceProtocol;

    public InboundJobExecutionDataProcessor(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundDiscoveryContextImpl inboundDiscoveryContext, com.energyict.mdc.upl.InboundDeviceProtocol inboundDeviceProtocol, OfflineDevice offlineDevice, ServiceProvider serviceProvider, InboundCommunicationHandler inboundCommunicationHandler, ComPortDiscoveryLogger logger, boolean executePendingTaskOnInboundConnection) {
        super(comPort, comServerDAO, deviceCommandExecutor, inboundDiscoveryContext, serviceProvider, inboundCommunicationHandler);
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
        GroupedDeviceCommand groupedDeviceCommand;
        try {
            Set<CollectedData> processedCollectedData = new HashSet<>();
            groupedDeviceCommand = root.getOrCreateGroupedDeviceCommand(offlineDevice, getDeviceProtocol(), null);
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                List<ProtocolTask> protocolTasksToExecute = new ArrayList<>(comTaskExecution.getProtocolTasks());

                for (ProtocolTask protocolTask : comTaskExecution.getProtocolTasks()) {
                    List<ServerCollectedData> dataForThisProtocolTask = receivedDataForProtocolTask(protocolTask, inboundDeviceProtocol.getCollectedData());
                    processedCollectedData.addAll(dataForThisProtocolTask);
                    if (dataForThisProtocolTask.isEmpty()) {
                        continue;
                    }
                    postProcess(dataForThisProtocolTask);
                    switch (ComCommandTypes.forProtocolTask(protocolTask.getClass())) {
                        case REGISTERS_COMMAND:
                            InboundCollectedRegisterCommandImpl inboundCollectedRegisterListCommand = new InboundCollectedRegisterCommandImpl(groupedDeviceCommand, (RegistersTask) protocolTask, comTaskExecution, dataForThisProtocolTask);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedRegisterListCommand);
                            protocolTasksToExecute.remove(protocolTask);
                            break;
                        case LOAD_PROFILE_COMMAND:
                            InboundCollectedLoadProfileCommandImpl inboundCollectedLoadProfileReadCommand = new InboundCollectedLoadProfileCommandImpl(groupedDeviceCommand, (LoadProfilesTask) protocolTask, comTaskExecution, dataForThisProtocolTask);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedLoadProfileReadCommand);
                            if (!(backFillEnabled() && requiredBackFillRead(inboundCollectedLoadProfileReadCommand, dataForThisProtocolTask))) {
                                logger.info("No back fill read required");
                                protocolTasksToExecute.remove(protocolTask);
                            }
                            break;
                        case LOGBOOKS_COMMAND:
                            InboundCollectedLogBookCommandImpl inboundCollectedLogBookReadCommand = new InboundCollectedLogBookCommandImpl(groupedDeviceCommand, (LogBooksTask) protocolTask, comTaskExecution, dataForThisProtocolTask);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedLogBookReadCommand);
                            protocolTasksToExecute.remove(protocolTask);
                            break;
                        case MESSAGES_COMMAND:
                            InboundCollectedMessageListCommandImpl inboundCollectedMessageListCommand = new InboundCollectedMessageListCommandImpl(groupedDeviceCommand, (MessagesTask) protocolTask, comTaskExecution, dataForThisProtocolTask);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedMessageListCommand);
                            protocolTasksToExecute.remove(protocolTask);
                            break;
                        case TOPOLOGY_COMMAND:
                            InboundCollectedTopologyCommandImpl inboundCollectedTopologyCommand = new InboundCollectedTopologyCommandImpl(groupedDeviceCommand, (TopologyTask) protocolTask, comTaskExecution, dataForThisProtocolTask);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedTopologyCommand);
                            protocolTasksToExecute.remove(protocolTask);
                            break;
                        case STATUS_INFORMATION_COMMAND:
                            InboundCollectedStatusInformationCommandImpl inboundCollectedStatusInformationCommandImpl = new InboundCollectedStatusInformationCommandImpl(groupedDeviceCommand, (StatusInformationTask) protocolTask, comTaskExecution, dataForThisProtocolTask);
                            addNewInboundComCommand(groupedDeviceCommand, comTaskExecution, inboundCollectedStatusInformationCommandImpl);
                            protocolTasksToExecute.remove(protocolTask);
                            break;
                    }
                }

                protocolTasksAlreadyExecutedForComTask.put(comTaskExecution, protocolTasksToExecute);
            }

            for (CollectedData collectedData : inboundDeviceProtocol.getCollectedData()) {
                if (!dataWasProcessed(processedCollectedData, collectedData) && !(collectedData instanceof DeviceConnectionProperty)) {
                    getInboundDiscoveryContext().markNotAllCollectedDataWasProcessed();
                    logDroppedDataOnComPortDiscoveryLogger(collectedData.getClass().getSimpleName());
                }
            }

        } catch (Throwable e) {
            root.generalSetupErrorOccurred(e, comTaskExecutions);
            root.removeAllGroupedDeviceCommands();
        }

        if (executePendingTaskOnInboundConnection) {
            //execute remaining pending tasks on same inbound connection
            return super.prepareAll(root, comTaskExecutions, false);
        } else {
            return root;
        }
    }

    @Override
    protected void prepareComTaskExecution(ComTaskExecution comTaskExecution, ComTaskExecutionConnectionSteps connectionSteps, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet, GroupedDeviceCommand groupedDeviceCommand, CommandCreator commandCreator) {
        final List<ProtocolTask> protocolTasks = protocolTasksAlreadyExecutedForComTask.getOrDefault(comTaskExecution, new ArrayList<>(comTaskExecution.getComTask().getProtocolTasks()));

        if (!protocolTasks.isEmpty() || hasConnectionSteps(connectionSteps)) {
            protocolTasks.sort(BasicCheckTasks.FIRST);

            commandCreator.createCommands(
                    groupedDeviceCommand,
                    getProtocolDialectTypedProperties(getConnectionTask()),
                    super.preparationContext.getComChannelPlaceHolder(),
                    protocolTasks,
                    deviceProtocolSecurityPropertySet,
                    connectionSteps,
                    comTaskExecution,
                    getServiceProvider().issueService());

            if (backFillEnabled()) {
                logger.log(Level.FINE, "Back-fill Enabled : ");
                replaceLoadProfileTaskWithBackFillTaskWhenExists(groupedDeviceCommand, comTaskExecution, protocolTasks);
            }
        }
    }

    private boolean hasConnectionSteps(ComTaskExecutionConnectionSteps steps) {
        return steps.isDaisyChainedLogOffRequired() || steps.isDaisyChainedLogOnRequired() || steps.isLogOffRequired() || steps.isLogOnRequired();
    }

    private boolean backFillEnabled() {
        return offlineDevice.getAllProperties().getTypedProperty(PROPERTY_BACK_FILL_ON_INBOUND, false);
    }

    private void replaceLoadProfileTaskWithBackFillTaskWhenExists(GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution, List<ProtocolTask> protocolTasks) {
        for (ProtocolTask task : protocolTasks) {
            if (task instanceof LoadProfilesTask) {
                logger.log(Level.FINE, "Replace load profile Task for Com-task " + comTaskExecution.getComTask().getName() + " with Back fill task");
                ComTaskExecutionComCommandImpl comTaskExecutionComCommand = groupedDeviceCommand.getComTaskRoot(comTaskExecution);
                comTaskExecutionComCommand.getCommands().put(ComCommandTypes.LOAD_PROFILE_COMMAND,
                        new BackFillLoadProfileCommandImpl(groupedDeviceCommand, (LoadProfilesTask) task, comTaskExecution, receivedDataForProtocolTask(task, inboundDeviceProtocol.getCollectedData())));
            }
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

    private List<ServerCollectedData> receivedDataForProtocolTask(ProtocolTask protocolTask, List<CollectedData> collectedData) {
        if (protocolTask instanceof RegistersTask) {
            return receivedRegisterDataForProtocolTask((RegistersTask) protocolTask, collectedData);
        } else if (protocolTask instanceof LoadProfilesTask) {
            return receivedLoadProfileDataForProtocolTask((LoadProfilesTask)protocolTask, collectedData);
        } else if (protocolTask instanceof LogBooksTask) {
            return receivedLogBookDataForProtocolTask((LogBooksTask) protocolTask, collectedData);
        } else if (protocolTask instanceof MessagesTask) {
            return receivedMessageDataForProtocolTask(collectedData);
        } else if (protocolTask instanceof TopologyTask) {
            return receivedTopologyDataForProtocolTask(collectedData);
        } else if (protocolTask instanceof StatusInformationTask) {
            return receivedStatusInformationTaskForProtocolTask(collectedData);
        }
        return Collections.emptyList();
    }

    private List<ServerCollectedData> receivedRegisterDataForProtocolTask(RegistersTask protocolTask, List<CollectedData> collectedData) {
        List<ServerCollectedData> data = new ArrayList<>();

        for (CollectedRegisterList collectedRegisterList : collectedData.stream().filter(CollectedRegisterList.class::isInstance).map(CollectedRegisterList.class::cast).collect(Collectors.toList())) {
            if (collectedRegisterList.getCollectedRegisters().stream()
                    .map(CollectedRegister::getRegisterIdentifier)
                    .map(RegisterIdentifier::getRegisterObisCode)
                    .anyMatch(obisCode -> protocolTask.getRegisterGroups()
                            .stream()
                            .map(RegisterGroup::getRegisterTypes)
                            .flatMap(List::stream)
                            .map(RegisterType::getObisCode)
                            .collect(Collectors.toList()).contains(obisCode))) {

                data.add((ServerCollectedData) collectedRegisterList);
            }
        }

        data.addAll(collectedData.stream()
                .filter(CollectedRegister.class::isInstance)
                .map(CollectedRegister.class::cast)
                .filter(collectedRegister -> protocolTask.getRegisterGroups()
                        .stream()
                        .map(RegisterGroup::getRegisterTypes)
                        .flatMap(List::stream)
                        .map(RegisterType::getObisCode)
                        .collect(Collectors.toList()).contains(collectedRegister.getRegisterIdentifier().getRegisterObisCode()))
                .filter(ServerCollectedData.class::isInstance)
                .map(ServerCollectedData.class::cast)
                .collect(Collectors.toList()));

        return data;
    }

    private List<ServerCollectedData> receivedLoadProfileDataForProtocolTask(LoadProfilesTask loadProfilesTask, List<CollectedData> collectedData) {
        return collectedData.stream()
                .filter(CollectedLoadProfile.class::isInstance)
                .map(CollectedLoadProfile.class::cast)
                .filter(collectedLoadProfile -> loadProfilesTask.getLoadProfileTypes().stream().map(LoadProfileType::getObisCode).collect(Collectors.toList()).contains(collectedLoadProfile.getLoadProfileIdentifier().getLoadProfileObisCode()))
                .filter(ServerCollectedData.class::isInstance)
                .map(ServerCollectedData.class::cast)
                .collect(Collectors.toList());
    }

    private List<ServerCollectedData> receivedLogBookDataForProtocolTask(LogBooksTask logBooksTask, List<CollectedData> collectedData) {
        return collectedData.stream()
                .filter(CollectedLogBook.class::isInstance)
                .map(CollectedLogBook.class::cast)
                .filter(collectedLogBook -> logBooksTask.getLogBookTypes().stream().map(LogBookType::getObisCode).collect(Collectors.toList()).contains(collectedLogBook.getLogBookIdentifier().getLogBookObisCode()))
                .filter(ServerCollectedData.class::isInstance)
                .map(ServerCollectedData.class::cast)
                .collect(Collectors.toList());
    }

    private List<ServerCollectedData> receivedMessageDataForProtocolTask(List<CollectedData> collectedData) {
        return Stream.concat(collectedData.stream()
                .filter(CollectedMessageList.class::isInstance), collectedData.stream()
                .filter(CollectedMessage.class::isInstance))
                .filter(ServerCollectedData.class::isInstance)
                .map(ServerCollectedData.class::cast)
                .collect(Collectors.toList());
    }

    private List<ServerCollectedData> receivedTopologyDataForProtocolTask(List<CollectedData> collectedData) {
        return collectedData.stream()
                .filter(CollectedTopology.class::isInstance)
                .filter(ServerCollectedData.class::isInstance)
                .map(ServerCollectedData.class::cast)
                .collect(Collectors.toList());
    }

    private List<ServerCollectedData> receivedStatusInformationTaskForProtocolTask(List<CollectedData> collectedData) {
        return collectedData.stream()
                .filter(CollectedFirmwareVersion.class::isInstance)
                .filter(ServerCollectedData.class::isInstance)
                .map(ServerCollectedData.class::cast)
                .collect(Collectors.toList());
    }

    private boolean requiredBackFillRead(LoadProfileCommand inboundCollectedLoadProfileReadCommand, List<ServerCollectedData> collectedData) {
        Device device = inboundCollectedLoadProfileReadCommand.getCommandRoot()
                .getServiceProvider()
                .deviceService()
                .findDeviceByIdentifier(offlineDevice.getDeviceIdentifier())
                .orElseThrow(() -> new NotFoundException("Could not resolve device identifier: '" + offlineDevice.getDeviceIdentifier().toString() + "'"));
        Optional<Instant> installedDate = device.getLifecycleDates().getInstalledDate();
        if (!installedDate.isPresent()) {
            logger.log(Level.WARNING, "Installation date hasn't been set. Disabling backfill. Please activate the device first.");
            return false;
        }

        for (LoadProfileReader reader : inboundCollectedLoadProfileReadCommand.getLoadProfileReaders()) {
            Optional<LoadProfile> loadProfileOptional = device.getLoadProfiles().stream().filter(lp -> lp.getObisCode().equals(reader.getProfileObisCode())).findFirst();
            Optional<CollectedLoadProfile> collectedLoadProfile = collectedData.stream().filter(CollectedLoadProfile.class::isInstance).map(CollectedLoadProfile.class::cast)
                    .filter(clp -> clp.getLoadProfileIdentifier().getLoadProfileObisCode().equals(reader.getProfileObisCode()))
                    .findFirst();
            if (loadProfileOptional.isPresent() && collectedLoadProfile.isPresent()) {
                Optional<Date> lastConsecutiveReadingDate = loadProfileOptional.get().getLastConsecutiveReading();
                if (!lastConsecutiveReadingDate.isPresent()) {
                    logger.log(Level.WARNING, "Last consecutive reading hasn't been set on the load profile " +  loadProfileOptional.get().getObisCode() + ". Disabling backfill.");
                    return false;
                }
                Instant lastConsecutiveReading = lastConsecutiveReadingDate.get().toInstant();

                AtomicReference<Instant> firstReceived = new AtomicReference(collectedLoadProfile.get().getCollectedIntervalDataRange().lowerEndpoint());
                offlineDevice.getAllOfflineLoadProfiles()
                        .stream()
                        .filter(offlineLoadProfile -> offlineLoadProfile.getObisCode().equals(reader.getProfileObisCode()))
                        .map(OfflineLoadProfile::getInterval)
                        .findFirst()
                        .ifPresent(interval ->  firstReceived.set(firstReceived.get().minus(interval)));

                if(lastConsecutiveReading.isBefore(firstReceived.get())) {
                    return true;
                }
            }
        }
        return false;
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

    private void logDroppedDataOnComPortDiscoveryLogger(String dataType) {
        comPortDiscoveryLogger.collectedDataWasFiltered(
                dataType,
                this.offlineDevice.getDeviceIdentifier(),
                getComPort());
    }

    protected DeviceProtocol getDeviceProtocol() {
        if (this.deviceProtocol == null) {
            DeviceProtocolPluggableClass protocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
            this.deviceProtocol = protocolPluggableClass.getDeviceProtocol();
        }
        return this.deviceProtocol;
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
        public NlsService nlsService() {
            return serviceProvider.nlsService();
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

        @Override
        public DeviceMessageService deviceMessageService() {
            return serviceProvider.deviceMessageService();
        }

        @Override
        public ProtocolPluggableService protocolPluggableService() {
            return serviceProvider.protocolPluggableService();
        }
    }
}