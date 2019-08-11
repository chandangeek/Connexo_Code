/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.VerifySerialNumberCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import com.energyict.protocol.exceptions.DeviceConfigurationException;

/**
 * Proper implementation for a {@link VerifySerialNumberCommand}
 *
 * @author gna
 * @since 31/05/12 - 16:06
 */
public class VerifySerialNumberCommandImpl extends SimpleComCommand implements VerifySerialNumberCommand {

    private final OfflineDevice offlineDevice;

    public VerifySerialNumberCommandImpl(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
        this.offlineDevice = groupedDeviceCommand.getOfflineDevice();
    }

    /**
     * Perform the actions which are owned by this ComCommand.<br/>
     * <b>Note:</b> this action will only perform once
     * <p>
     * The serialNumber will be read from the device and verified with the serialNumber from the HeadEnd.
     * If the serialNumbers don't match, then a {@link DeviceConfigurationException} will be thrown.
     * </p>
     *
     * @param deviceProtocol   the {@link DeviceProtocol} which will perform the actions
     * @param executionContext The ExecutionContext
     */
    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        String meterSerialNumber = deviceProtocol.getSerialNumber();
        if (meterSerialNumber == null) {
            addIssue(
                    getIssueService().newWarning(deviceProtocol, MessageSeeds.NOT_POSSIBLE_TO_VERIFY_SERIALNUMBER, offlineDevice.getSerialNumber(), deviceProtocol.getClass().getSimpleName()),
                    CompletionCode.ConfigurationWarning
            );
            // strips leading 0's from both serial numbers
        } else if (!meterSerialNumber.replaceAll("^0*", "").equals(offlineDevice.getSerialNumber().replaceAll("^0*", ""))) {
            addIssue(getIssueService().newProblem(getCommandType(), MessageSeeds.CONFIG_SERIAL_NUMBER_MISMATCH, meterSerialNumber, offlineDevice.getSerialNumber()), CompletionCode.ConfigurationError);
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Verify the device serial number";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND;
    }

}