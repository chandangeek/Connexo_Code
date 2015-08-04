package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.CreateComTaskExecutionSessionCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLogBookCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedMessageListCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedRegisterCommandImpl;
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
import com.energyict.mdc.tasks.*;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final InboundDeviceProtocol inboundDeviceProtocol;
    private final OfflineDevice offlineDevice;
    private final ServiceProvider serviceProvider;
    private DeviceProtocol deviceProtocol;

    public InboundJobExecutionDataProcessor(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundDiscoveryContextImpl inboundDiscoveryContext, InboundDeviceProtocol inboundDeviceProtocol, OfflineDevice offlineDevice, ServiceProvider serviceProvider, InboundCommunicationHandler inboundCommunicationHandler) {
        super(comPort, comServerDAO, deviceCommandExecutor, inboundDiscoveryContext, serviceProvider, inboundCommunicationHandler);
        this.inboundDeviceProtocol = inboundDeviceProtocol;
        this.offlineDevice = offlineDevice;
        this.serviceProvider = serviceProvider;
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
        boolean success = false;
        try {
            Future<Boolean> future = super.doExecuteStoreCommand();
            try {
                success = future.get();        //Blocking call until the device command is fully executed (= collected data is stored)
            } catch (InterruptedException e) {
                success = false;
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                //Should never happen, since the Worker (Callable) already catches Throwable
                success = false;
            }
            return future;
        } finally {
            inboundCommunicationHandler.provideResponse(inboundDeviceProtocol, success ? InboundDeviceProtocol.DiscoverResponseType.SUCCESS : InboundDeviceProtocol.DiscoverResponseType.STORING_FAILURE);
        }
    }

    @Override
    protected List<PreparedComTaskExecution> prepareAll(List<? extends ComTaskExecution> comTaskExecutions) {
        List<PreparedComTaskExecution> allPreparedComTaskExecutions = new ArrayList<>();
        CommandRoot root = new CommandRootImpl(this.offlineDevice, getExecutionContext(), new CommandRootServiceProvider(), true);
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            List<ServerCollectedData> data = this.receivedCollectedDataFor(comTaskExecution);
            if (!data.isEmpty()) {
                this.postProcess(data);
                addToRoot(new CreateComTaskExecutionSessionCommandImpl(
                        new CreateComTaskExecutionSessionTask(getFirstComTask(comTaskExecution), comTaskExecution), root, comTaskExecution), root, comTaskExecution);
                this.supportedComCommandTypes()
                        .map(commandType -> this.findProtocolTask(comTaskExecution, commandType))
                        .flatMap(Functions.asStream())
                        .forEach(protocolTask -> this.addCommandFor(comTaskExecution, protocolTask, root, data));

                allPreparedComTaskExecutions.add(new PreparedComTaskExecution(comTaskExecution, root, getDeviceProtocol()));
            }
        }
        return allPreparedComTaskExecutions;
    }

    private ComTask getFirstComTask(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getComTasks().get(0);
    }

    private Stream<ComCommandTypes> supportedComCommandTypes() {
        return Stream.of(ComCommandTypes.MESSAGES_COMMAND, ComCommandTypes.LOGBOOKS_COMMAND, ComCommandTypes.LOAD_PROFILE_COMMAND, ComCommandTypes.REGISTERS_COMMAND);
    }

    private Optional<ProtocolTask> findProtocolTask(ComTaskExecution comTaskExecution, ComCommandTypes commandType) {
        for (ProtocolTask protocolTask : comTaskExecution.getProtocolTasks()) {
            if (commandType.equals(ComCommandTypes.forProtocolTask(protocolTask))) {
                return Optional.of(protocolTask);
            }
        }
        return Optional.empty();
    }

    private void addCommandFor(ComTaskExecution comTaskExecution, ProtocolTask protocolTask, CommandRoot root, List<ServerCollectedData> data) {
        ComCommand command;
        if (ComCommandTypes.MESSAGES_COMMAND.appliesTo(protocolTask)) {
            command = new InboundCollectedMessageListCommandImpl(((MessagesTask) protocolTask), this.offlineDevice, root, data, comTaskExecution);
            this.addToRoot(command, root, comTaskExecution);
        }
        else if (ComCommandTypes.LOGBOOKS_COMMAND.appliesTo(protocolTask)) {
            command = new InboundCollectedLogBookCommandImpl((LogBooksTask) protocolTask, this.offlineDevice, root, comTaskExecution, data, this.serviceProvider.deviceService());
            this.addToRoot(command, root, comTaskExecution);
        }
        else if (ComCommandTypes.LOAD_PROFILE_COMMAND.appliesTo(protocolTask)) {
            command = new InboundCollectedLoadProfileCommandImpl((LoadProfilesTask) protocolTask, this.offlineDevice, root, comTaskExecution, data);
        }
        else {  // Must be ComCommandTypes.REGISTERS_COMMAND
            command = new InboundCollectedRegisterCommandImpl((RegistersTask) protocolTask, this.offlineDevice, root, comTaskExecution, data, this.serviceProvider.deviceService());
        }
        this.addToRoot(command, root, comTaskExecution);
    }

    private void postProcess(List<ServerCollectedData> data) {
        for (ServerCollectedData collectedData : data) {
            collectedData.postProcess(getConnectionTask());
        }
    }

    private void addToRoot(ComCommand comCommand, CommandRoot root, ComTaskExecution comTaskExecution) {
        root.addUniqueCommand(comCommand, comTaskExecution);
    }

    protected DeviceProtocol getDeviceProtocol() {
        if (this.deviceProtocol == null) {
            DeviceProtocolPluggableClass protocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
            this.deviceProtocol = protocolPluggableClass.getDeviceProtocol();
        }
        return this.deviceProtocol;
    }

    private List<ServerCollectedData> receivedCollectedDataFor(ComTaskExecution comTaskExecution) {
        return inboundDeviceProtocol.getCollectedData(this.offlineDevice)
                .stream()
                .filter(collectedData -> collectedData.isConfiguredIn(comTaskExecution))
                .map(ServerCollectedData.class::cast)
                .collect(Collectors.toList());
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
        public LogBookService logBookService() {
            return serviceProvider.logBookService();
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
