/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.TopologyTask;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.TopologyCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceConnectionProperty;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDeviceCache;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.Collection;
import java.util.List;

public class InboundCollectedTopologyCommandImpl extends TopologyCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedTopologyCommandImpl(GroupedDeviceCommand groupedDeviceCommand, TopologyTask topologyTask, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData) {
        super(groupedDeviceCommand, topologyTask.getTopologyAction(), comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        collectedData.stream().filter(dataItem -> dataItem instanceof CollectedTopology || dataItem instanceof DeviceConnectionProperty || dataItem instanceof CollectedDeviceCache).forEach(this::addCollectedDataItem);
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (getCollectedTopology() == null) {
            builder.addLabel("No Topology info collected");
        } else {
            builder.addProperty("topologyAction").append(getTopologyAction().name());
            if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
                this.appendTopology(builder, getCollectedTopology());
            }
        }
    }

    private void appendTopology(DescriptionBuilder builder, CollectedTopology deviceTopology) {
        StringBuilder topologyActionResultBuilder;
        switch (deviceTopology.getTopologyAction()) {
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
        topologyActionResultBuilder.append(deviceTopology.getDeviceIdentifier());
        PropertyDescriptionBuilder originalSlavesBuilder = builder.addListProperty("originalSlaves");
        appendSlaves(originalSlavesBuilder, getSlaveIdentifiersFromOfflineDevices());
        PropertyDescriptionBuilder receivedSlavesBuilder = builder.addListProperty("receivedSlaves");
        appendSlaves(receivedSlavesBuilder, deviceTopology.getSlaveDeviceIdentifiers().keySet());
        appendCollectedDeviceInfo(builder, deviceTopology.getAdditionalCollectedDeviceInfo());
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

    private void appendSlaves(PropertyDescriptionBuilder builder, Collection<DeviceIdentifier> slaveDeviceIdentifiers) {
        if (slaveDeviceIdentifiers.isEmpty()) {
            builder.append("None").next();
        } else {
            for (DeviceIdentifier slaveDeviceIdentifier : slaveDeviceIdentifiers) {
                builder = builder.append(slaveDeviceIdentifier).next();
            }
        }
    }

    private CollectedTopology getCollectedTopology() {
        for (CollectedData data : getCollectedData()) {
            if (data instanceof CollectedTopology) {
                return (CollectedTopology) data;
            }
        }
        return null;
    }
}
