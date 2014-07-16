package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedLogBookCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound.InboundCollectedRegisterCommandImpl;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Clock;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link InboundJobExecutionDataProcessor} is responsible for
 * processing collected data which was received during inbound communication.
 * A logical <i>connect</i> can be skipped as the
 * {@link ComChannel ComChannl}
 * will already be created by the ComPortListener
 * <p/>
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
            List<ServerCollectedData> data = receivedCollectedDataFor(comTaskExecution);
            if (!data.isEmpty()) {
                postProcess(data);
                ProtocolTask registersTask = getRegistersTask(comTaskExecution);
                ProtocolTask loadProfilesTask = getLoadProfilesTask(comTaskExecution);
                ProtocolTask logBooksTask = getLogBooksTask(comTaskExecution);
                ProtocolTask messageTask = getMessageTask(comTaskExecution);
                if (registersTask != null) {
                    InboundCollectedRegisterCommandImpl inboundCollectedRegisterListCommand = new InboundCollectedRegisterCommandImpl((RegistersTask) registersTask, offlineDevice, root, comTaskExecution, data, serviceProvider.deviceDataService());
                    addNewInboundComCommand(allPreparedComTaskExecutions, root, comTaskExecution, inboundCollectedRegisterListCommand);
                } else if (loadProfilesTask != null) {
                    InboundCollectedLoadProfileCommandImpl inboundCollectedLoadProfileReadCommand = new InboundCollectedLoadProfileCommandImpl((LoadProfilesTask) loadProfilesTask, offlineDevice, root, comTaskExecution, data);
                    addNewInboundComCommand(allPreparedComTaskExecutions, root, comTaskExecution, inboundCollectedLoadProfileReadCommand);
                } else if (logBooksTask != null) {
                    InboundCollectedLogBookCommandImpl inboundCollectedLogBookReadCommand = new InboundCollectedLogBookCommandImpl((LogBooksTask) logBooksTask, offlineDevice, root, comTaskExecution, data, serviceProvider.deviceDataService());
                    addNewInboundComCommand(allPreparedComTaskExecutions, root, comTaskExecution, inboundCollectedLogBookReadCommand);

                /*Todo reenable onces Messages are ported
                } else if (messageTask != null) {
                    InboundCollectedMessageListCommandImpl inboundCollectedMessageListCommand = new InboundCollectedMessageListCommandImpl((ServerMessagesTask) messageTask, offlineDevice, root, data);
                    addNewInboundComCommand(allPreparedComTaskExecutions, root, comTaskExecution, inboundCollectedMessageListCommand);
                 */
                }
            }
        }
        return allPreparedComTaskExecutions;
    }

    private void postProcess(List<ServerCollectedData> data) {
        for (ServerCollectedData collectedData : data) {
            collectedData.postProcess(getConnectionTask());
        }
    }

    private void addNewInboundComCommand(List<PreparedComTaskExecution> allPreparedComTaskExecutions, CommandRoot root, ComTaskExecution comTaskExecution, ComCommand comCommand) {
        root.addCommand(comCommand, comTaskExecution);
        getInboundDiscoveryContext().getComSessionBuilder().incrementNotExecutedTasks();
        allPreparedComTaskExecutions.add(new PreparedComTaskExecution(comTaskExecution, root, getDeviceProtocol()));
    }

    private ProtocolTask getMessageTask(ComTaskExecution comTaskExecution){
        for (ProtocolTask protocolTask : comTaskExecution.getProtocolTasks()) {
            //TODO reenable onces messages are ported
//            if (ComCommandTypes.MESSAGES_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
//                return protocolTask;
//            }
        }
        return null;
    }

    private ProtocolTask getLogBooksTask(ComTaskExecution comTaskExecution){
        for (ProtocolTask protocolTask : comTaskExecution.getProtocolTasks()) {
            if (ComCommandTypes.LOGBOOKS_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                return protocolTask;
            }
        }
        return null;
    }


    private ProtocolTask getLoadProfilesTask(ComTaskExecution comTaskExecution){
        for (ProtocolTask protocolTask : comTaskExecution.getProtocolTasks()) {
            if (ComCommandTypes.LOAD_PROFILE_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                return protocolTask;
            }
        }
        return null;
    }

    private ProtocolTask getRegistersTask(ComTaskExecution comTaskExecution){
        for (ProtocolTask protocolTask : comTaskExecution.getProtocolTasks()) {
            if (ComCommandTypes.REGISTERS_COMMAND.equals(ComCommandTypes.forProtocolTask(protocolTask.getClass()))) {
                return protocolTask;
            }
        }
        return null;
    }

    private DeviceProtocol getDeviceProtocol() {
        if (this.deviceProtocol == null) {
            DeviceProtocolPluggableClass protocolPluggableClass = offlineDevice.getDeviceProtocolPluggableClass();
            this.deviceProtocol = protocolPluggableClass.getDeviceProtocol();
        }
        return this.deviceProtocol;
    }

    private List<ServerCollectedData> receivedCollectedDataFor(ComTaskExecution comTaskExecution) {
        List<ServerCollectedData> collectedDatas = new ArrayList<>();
        for (CollectedData collectedData : inboundDeviceProtocol.getCollectedData()) {
            if (collectedData.isConfiguredIn(comTaskExecution)) {
                collectedDatas.add((ServerCollectedData) collectedData);
            }
        }
        return collectedDatas;
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
        public DeviceDataService deviceDataService() {
            return serviceProvider.deviceDataService();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public TaskHistoryService taskHistoryService() {
            return serviceProvider.taskHistoryService();
        }

        @Override
        public TransactionService transactionService() {
            return serviceProvider.transactionService();
        }

    }

}
