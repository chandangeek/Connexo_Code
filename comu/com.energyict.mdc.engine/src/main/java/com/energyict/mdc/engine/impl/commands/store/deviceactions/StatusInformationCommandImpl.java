/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.StatusInformationCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

/**
 * Implementation for a {@link StatusInformationCommand}<br/>
 * The collected data will be predefined status {@link com.energyict.mdc.protocol.api.device.BaseRegister}s.
 * The actual reading of the registers will be performed by the {@link ReadRegistersCommand}
 *
 * @author gna
 * @since 18/06/12 - 8:39
 */
public class StatusInformationCommandImpl extends SimpleComCommand implements StatusInformationCommand {

    private final ComTaskExecution comTaskExecution;

    public StatusInformationCommandImpl(final OfflineDevice device, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        this.comTaskExecution = comTaskExecution;
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (groupedDeviceCommand == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "groupedDeviceCommand", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Read the device status information";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.STATUS_INFORMATION_COMMAND;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if(getOfflineDevice().firmwareVersionManagementAllowed()){
            CollectedFirmwareVersion firmwareVersions = deviceProtocol.getFirmwareVersions();
            firmwareVersions.setDataCollectionConfiguration(comTaskExecution);
            addCollectedDataItem(firmwareVersions);
        }

        CollectedBreakerStatus breakerStatus = deviceProtocol.getBreakerStatus();
        breakerStatus.setDataCollectionConfiguration(comTaskExecution);
        addCollectedDataItem(breakerStatus);

        if(getOfflineDevice().touCalendarManagementAllowed()){
            CollectedCalendar calendar = deviceProtocol.getCollectedCalendar();
            calendar.setDataCollectionConfiguration(comTaskExecution);
            addCollectedDataItem(calendar);
        }
    }

}