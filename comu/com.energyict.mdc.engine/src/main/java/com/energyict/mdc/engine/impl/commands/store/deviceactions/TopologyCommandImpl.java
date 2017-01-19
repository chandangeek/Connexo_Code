package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.TopologyCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.TopologyTask;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.TopologyAction;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link TopologyCommand}
 *
 * @author gna
 * @since 9/05/12 - 15:49
 */
public class TopologyCommandImpl extends SimpleComCommand implements TopologyCommand {

    private final OfflineDevice offlineDevice;
    private final ComTaskExecution comTaskExecution;
    /**
     * The {@link TopologyAction} that must be taken when executing the command.
     */
    private TopologyAction topologyAction;
    private CollectedTopology deviceTopology;

    public TopologyCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, TopologyAction topologyAction, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        this.topologyAction = topologyAction;
        this.offlineDevice = groupedDeviceCommand.getOfflineDevice();
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public void updateAccordingTo(TopologyTask topologyTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (!this.topologyAction.equals(topologyTask.getTopologyAction()) && topologyTask.getTopologyAction().equals(TopologyAction.UPDATE)) {
            this.topologyAction = topologyTask.getTopologyAction(); // 'Verify' action is overruled by 'Update' action
        }
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.TOPOLOGY_COMMAND;
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.deviceTopology = deviceProtocol.getDeviceTopology();
        this.deviceTopology.setTopologyAction(this.topologyAction);
        ((ServerCollectedData) this.deviceTopology).injectComTaskExecution(this.comTaskExecution);
        this.deviceTopology.getAdditionalCollectedDeviceInfo().stream().forEach(collectedDeviceInfo -> ((ServerCollectedData) collectedDeviceInfo).injectComTaskExecution(comTaskExecution));
        addCollectedDataItem(this.deviceTopology);
    }

    protected List<DeviceIdentifier> getSlaveIdentifiersFromOfflineDevices() {
        return this.offlineDevice.getAllSlaveDevices().stream().map(OfflineDevice::getDeviceIdentifier).collect(Collectors.toList());
    }

    public TopologyAction getTopologyAction() {
        return topologyAction;
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed topology protocol task";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("topologyAction").append(this.topologyAction.name());
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            if (this.deviceTopology != null) {
                this.appendTopology(builder);
            }
        }
    }

    private void appendTopology(DescriptionBuilder builder) {
        StringBuilder topologyActionResultBuilder;
        switch (this.deviceTopology.getTopologyAction()) {
            case UPDATE: {
                topologyActionResultBuilder = builder.addProperty("updatedTopologyMaster");
                break;
            }
            case VERIFY:
            default: {
                topologyActionResultBuilder = builder.addProperty("verifiedTopologyMaster");
                break;
            }
        }
        topologyActionResultBuilder.append(this.deviceTopology.getDeviceIdentifier());
        PropertyDescriptionBuilder originalSlavesBuilder = builder.addListProperty("originalSlaves");
        appendSlaves(originalSlavesBuilder, getSlaveIdentifiersFromOfflineDevices());
        PropertyDescriptionBuilder receivedSlavesBuilder = builder.addListProperty("receivedSlaves");
        appendSlaves(receivedSlavesBuilder, this.deviceTopology.getSlaveDeviceIdentifiers().keySet());
        appendCollectedDeviceInfo(builder, this.deviceTopology.getAdditionalCollectedDeviceInfo());
    }

    private void appendSlaves(PropertyDescriptionBuilder builder, Collection<DeviceIdentifier> slaveDeviceIdentifiers) {
        if (slaveDeviceIdentifiers.isEmpty()) {
            builder.append("None").next();
        } else {
            for (DeviceIdentifier slaveDeviceIdentifier : slaveDeviceIdentifiers) {
                builder = builder.append(slaveDeviceIdentifier).next();
            }
        }
    }

    private void appendCollectedDeviceInfo(DescriptionBuilder builder, List<CollectedDeviceInfo> additionalCollectedDeviceInfo) {
        if (!additionalCollectedDeviceInfo.isEmpty()) {
            PropertyDescriptionBuilder deviceInfoListBuilder = builder.addListProperty("additionalDeviceInfo");
            for (CollectedDeviceInfo collectedDeviceInfo : additionalCollectedDeviceInfo) {
                deviceInfoListBuilder.append(collectedDeviceInfo.toString());
                deviceInfoListBuilder.next();
            }
        }
    }
}
