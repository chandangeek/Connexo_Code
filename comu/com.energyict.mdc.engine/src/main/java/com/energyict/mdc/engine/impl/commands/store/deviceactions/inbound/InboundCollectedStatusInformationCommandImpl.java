/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.StatusInformationCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;

import java.util.List;

public class InboundCollectedStatusInformationCommandImpl extends StatusInformationCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedStatusInformationCommandImpl(GroupedDeviceCommand groupedDeviceCommand, StatusInformationTask statusInformationTask, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData) {
        super(groupedDeviceCommand.getOfflineDevice(), groupedDeviceCommand, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        collectedData.stream()
                .filter(dataItem -> dataItem instanceof CollectedFirmwareVersion || dataItem instanceof CollectedBreakerStatus || dataItem instanceof CollectedCreditAmount || dataItem instanceof CollectedCalendar)
                .forEach(this::addCollectedDataItem);
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (getCollectedFirmwareVersion() == null) {
            builder.addLabel("No FirmwareVersion collected");
        }
    }

    private void appendFirmwareVersion(DescriptionBuilder builder, CollectedFirmwareVersion collectedFirmwareVersion) {
        StringBuilder firmwareVersionBuilder = new StringBuilder();
        firmwareVersionBuilder.append(collectedFirmwareVersion.getDeviceIdentifier());
        firmwareVersionBuilder.append(collectedFirmwareVersion.getActiveMeterFirmwareVersion());
    }

    private CollectedFirmwareVersion getCollectedFirmwareVersion() {
        for (CollectedData data : getCollectedData()) {
            if (data instanceof CollectedFirmwareVersion) {
                return (CollectedFirmwareVersion) data;
            }
        }
        return null;
    }
}
