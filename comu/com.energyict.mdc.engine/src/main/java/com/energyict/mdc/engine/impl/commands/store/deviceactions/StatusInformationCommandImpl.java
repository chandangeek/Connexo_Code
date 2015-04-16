package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.StatusInformationCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.meterdata.DeviceFirmwareVersion;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import java.util.Optional;

/**
 * Implementation for a {@link StatusInformationCommand}<br/>
 * The collected data will be predefined status {@link com.energyict.mdc.protocol.api.device.BaseRegister}s.
 * The actual reading of the registers will be performed by the {@link ReadRegistersCommand}
 *
 * @author gna
 * @since 18/06/12 - 8:39
 */
public class StatusInformationCommandImpl extends SimpleComCommand implements StatusInformationCommand {

    private final DeviceIdentifier<?> deviceDeviceIdentifier;

    public StatusInformationCommandImpl(final OfflineDevice device, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device");
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot");
        }
        this.deviceDeviceIdentifier = device.getDeviceIdentifier();
    }

    @Override
    public String getDescriptionTitle() {
        return "Read out the device status information";
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.STATUS_INFORMATION_COMMAND;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        addCollectedDataItem(deviceProtocol.getFirmwareVersions());
    }
}