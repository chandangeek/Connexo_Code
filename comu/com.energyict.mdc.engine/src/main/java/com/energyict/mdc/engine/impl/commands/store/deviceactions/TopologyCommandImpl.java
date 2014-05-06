package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TopologyCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import com.energyict.protocolimplv2.identifiers.SerialNumberDeviceIdentifier;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.tasks.TopologyAction.*;

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
    public void doExecute(final DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        this.deviceTopology = deviceProtocol.getDeviceTopology();
        this.deviceTopology.setTopologyAction(this.topologyAction);
        this.deviceTopology.setDataCollectionConfiguration(this.comTaskExecution);
        addCollectedDataItem(this.deviceTopology);
    }

    private List<DeviceIdentifier> getSlaveIdentifiersFromOfflineDevices() {
        List<DeviceIdentifier> slaveIdentifiers = new ArrayList<>();
        for (OfflineDevice device : this.offlineDevice.getAllSlaveDevices()) {
            slaveIdentifiers.add(new SerialNumberDeviceIdentifier(device.getSerialNumber()));
        }
        return slaveIdentifiers;
    }

    public TopologyAction getTopologyAction() {
        return topologyAction;
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

}
