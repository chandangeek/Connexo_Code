package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.StatusInformationCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.List;

/**
 * Implementation for a {@link StatusInformationCommand}<br/>
 * The collected data will be predefined status {@link com.energyict.mdc.protocol.api.device.BaseRegister}s.
 * The actual reading of the registers will be performed by the {@link ReadRegistersCommand}
 *
 * @author gna
 * @since 18/06/12 - 8:39
 */
public class StatusInformationCommandImpl extends CompositeComCommandImpl implements StatusInformationCommand {

    public StatusInformationCommandImpl(final OfflineDevice device, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device");
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot");
        }

        //TODO depending on how the MDM team will implement the status:
        // - either by a fixed RegisterGroup
        // - or with a marker on each register, meaning this will be a status Register
        List<OfflineRegister> registers = null;

        ReadRegistersCommand readRegistersCommand = getCommandRoot().getReadRegistersCommand(this, comTaskExecution);
        readRegistersCommand.addRegisters(registers);
    }

    /**
     * @return the ComCommandTypes of this command
     */
    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.STATUS_INFORMATION_COMMAND;
    }

}