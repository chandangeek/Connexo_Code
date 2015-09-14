package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TopologyCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link TopologyCommand}
 *
 * @author gna
 * @since 9/05/12 - 15:49
 */
public class TopologyCommandImpl extends SimpleComCommand implements TopologyCommand {

    /**
     * The {@link TopologyAction} that must be taken when executing the command.
     */
    private final TopologyAction topologyAction;
    private final OfflineDevice offlineDevice;
    private final ComTaskExecution comTaskExecution;
    private CollectedTopology deviceTopology;

    public TopologyCommandImpl(final CommandRoot commandRoot, TopologyAction topologyAction, OfflineDevice offlineDevice, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        this.topologyAction = topologyAction;
        this.offlineDevice = offlineDevice;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.TOPOLOGY_COMMAND;
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.deviceTopology = deviceProtocol.getDeviceTopology();
        this.deviceTopology.setTopologyAction(this.topologyAction);
        this.deviceTopology.setDataCollectionConfiguration(this.comTaskExecution);
        this.deviceTopology.getAdditionalCollectedDeviceInfo().stream().forEach(collectedDeviceInfo -> collectedDeviceInfo.setDataCollectionConfiguration(comTaskExecution));
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
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addProperty("topologyAction").append(this.topologyAction.name());
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            if (this.deviceTopology != null) {
                this.appendTopology(builder);
            }
        }
    }

    private void appendTopology (DescriptionBuilder builder) {
        StringBuilder topologyActionResultBuilder = builder.addProperty("topologyActionResult");
        switch (this.deviceTopology.getTopologyAction()) {
            case UPDATE: {
                topologyActionResultBuilder = builder.addProperty("updatedTopologyMaster");
                break;
            }
            case VERIFY: {
                topologyActionResultBuilder = builder.addProperty("verifiedTopologyMaster");
                break;
            }
        }
        topologyActionResultBuilder.append(this.deviceTopology.getDeviceIdentifier());
        PropertyDescriptionBuilder originalSlavesBuilder = builder.addListProperty("originalSlaves");
        appendSlaves(originalSlavesBuilder, getSlaveIdentifiersFromOfflineDevices());
        PropertyDescriptionBuilder receivedSlavesBuilder = builder.addListProperty("receivedSlaves");
        appendSlaves(receivedSlavesBuilder, this.deviceTopology.getSlaveDeviceIdentifiers());
        appendCollectedDeviceInfo(builder, this.deviceTopology.getAdditionalCollectedDeviceInfo());
    }

    private void appendSlaves(PropertyDescriptionBuilder builder, List<DeviceIdentifier> slaveDeviceIdentifiers) {
        if (slaveDeviceIdentifiers.isEmpty()) {
            builder.append("None").next();
        }
        else {
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
