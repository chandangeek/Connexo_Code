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
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLogBookCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedMessageListCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedRegisterCommandImpl;
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
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public InboundJobExecutionDataProcessor(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundDiscoveryContextImpl inboundDiscoveryContext, InboundDeviceProtocol inboundDeviceProtocol, OfflineDevice offlineDevice, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, inboundDiscoveryContext, serviceProvider);
        this.inboundDeviceProtocol = inboundDeviceProtocol;
        this.offlineDevice = offlineDevice;
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void setExecutionContext(ExecutionContext executionContext) {
        executionContext.setComSessionShadow(getInboundDiscoveryContext().getComSessionBuilder());
        super.setExecutionContext(executionContext);
    }

    @Override
    protected List<PreparedComTaskExecution> prepareAll(List<? extends ComTaskExecution> comTaskExecutions) {
        List<PreparedComTaskExecution> allPreparedComTaskExecutions = new ArrayList<>();
        CommandRoot root = new CommandRootImpl(this.offlineDevice, getExecutionContext(), new CommandRootServiceProvider());
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            List<ServerCollectedData> data = this.receivedCollectedDataFor(comTaskExecution);
            if (!data.isEmpty()) {
                this.postProcess(data);
                allPreparedComTaskExecutions.addAll(
                        this.supportedComCommandTypes()
                                .map(commandType -> this.findProtocolTask(comTaskExecution, commandType))
                                .flatMap(Functions.asStream())
                                .map(protocolTask -> this.addCommandFor(comTaskExecution, protocolTask, root, data))
                                .collect(Collectors.toList()));
            }
        }
        return allPreparedComTaskExecutions;
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

    private PreparedComTaskExecution addCommandFor(ComTaskExecution comTaskExecution, ProtocolTask protocolTask, CommandRoot root, List<ServerCollectedData> data) {
        ComCommand command;
        if (ComCommandTypes.MESSAGES_COMMAND.appliesTo(protocolTask)) {
            command = new InboundCollectedMessageListCommandImpl(((MessagesTask) protocolTask), this.offlineDevice, root, data, comTaskExecution);
            return this.prepare(command, root, comTaskExecution);
        }
        else if (ComCommandTypes.LOGBOOKS_COMMAND.appliesTo(protocolTask)) {
            command = new InboundCollectedLogBookCommandImpl((LogBooksTask) protocolTask, this.offlineDevice, root, comTaskExecution, data, this.serviceProvider.deviceService());
            return this.prepare(command, root, comTaskExecution);
        }
        else if (ComCommandTypes.LOAD_PROFILE_COMMAND.appliesTo(protocolTask)) {
            command = new InboundCollectedLoadProfileCommandImpl((LoadProfilesTask) protocolTask, this.offlineDevice, root, comTaskExecution, data);
        }
        else {  // Must be ComCommandTypes.REGISTERS_COMMAND
            command = new InboundCollectedRegisterCommandImpl((RegistersTask) protocolTask, this.offlineDevice, root, comTaskExecution, data, this.serviceProvider.deviceService());
        }
        return this.prepare(command, root, comTaskExecution);
    }

    private void postProcess(List<ServerCollectedData> data) {
        for (ServerCollectedData collectedData : data) {
            collectedData.postProcess(getConnectionTask());
        }
    }

    private PreparedComTaskExecution prepare(ComCommand comCommand, CommandRoot root, ComTaskExecution comTaskExecution) {
        root.addCommand(comCommand, comTaskExecution);
        this.getInboundDiscoveryContext().getComSessionBuilder().incrementNotExecutedTasks();
        return new PreparedComTaskExecution(comTaskExecution, root, getDeviceProtocol());
    }

    private DeviceProtocol getDeviceProtocol() {
        if (this.deviceProtocol == null) {
            DeviceProtocolPluggableClass protocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
            this.deviceProtocol = protocolPluggableClass.getDeviceProtocol();
        }
        return this.deviceProtocol;
    }

    private List<ServerCollectedData> receivedCollectedDataFor(ComTaskExecution comTaskExecution) {
        return inboundDeviceProtocol.getCollectedData()
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

    }

}
