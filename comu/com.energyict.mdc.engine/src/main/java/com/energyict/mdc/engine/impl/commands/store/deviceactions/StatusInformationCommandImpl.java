/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.StatusInformationCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.meterdata.*;

import java.util.Optional;

/**
 * Implementation for a {@link StatusInformationCommand}<br/>
 * The collected data will be predefined status {@link com.energyict.mdc.upl.meterdata.Register}s.
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
        final String deviceSerialNumber = comTaskExecution.getDevice().getSerialNumber();
        if (isFirmwareVersionManagementAllowed(getOfflineDevice(), deviceSerialNumber)) {
            CollectedFirmwareVersion firmwareVersions = deviceProtocol.getFirmwareVersions(comTaskExecution.getDevice().getSerialNumber());
            if (firmwareVersions != null) {
                firmwareVersions.setDataCollectionConfiguration(comTaskExecution);
                addCollectedDataItem(firmwareVersions);
            } else {
                addIssue(getIssueService().newWarning(this, com.energyict.mdc.engine.impl.commands.MessageSeeds.FIRMWARE_INFORMATION_NOT_SUPPORTED), CompletionCode.ConfigurationError);
            }
        }

        CollectedBreakerStatus breakerStatus = deviceProtocol.getBreakerStatus();
        breakerStatus.setDataCollectionConfiguration(comTaskExecution);
        addCollectedDataItem(breakerStatus);

        CollectedCreditAmount creditAmount = deviceProtocol.getCreditAmount();
        creditAmount.setDataCollectionConfiguration(comTaskExecution);
        addCollectedDataItem(creditAmount);

        if (getOfflineDevice().touCalendarManagementAllowed() && getOfflineDevice().getSerialNumber().equals(deviceSerialNumber)) {
            CollectedCalendar calendar = deviceProtocol.getCollectedCalendar();
            calendar.setDataCollectionConfiguration(comTaskExecution);
            addCollectedDataItem(calendar);
        }
    }

    private boolean isFirmwareVersionManagementAllowed(OfflineDevice offlineDevice, String deviceSerialNumber) {
        if (offlineDevice.getSerialNumber().equals(deviceSerialNumber)) {
            return offlineDevice.firmwareVersionManagementAllowed();
        }
        // task was executed from slave device, check the slave
        final Optional<? extends com.energyict.mdc.upl.offline.OfflineDevice> slave = offlineDevice.getAllSlaveDevices().stream()
                .filter(d -> d.getSerialNumber().equals(deviceSerialNumber)).findFirst();
        return slave.map(com.energyict.mdc.upl.offline.OfflineDevice::firmwareVersionManagementAllowed).orElse(false);
    }

}