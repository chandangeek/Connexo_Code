package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for a {@link ReadRegistersCommand}
 *
 * @author gna
 * @since 18/06/12 - 13:39
 */
public class ReadRegistersCommandImpl extends SimpleComCommand implements ReadRegistersCommand {

    private CompositeComCommand commandOwner;

    /**
     * The list off Registers to read from the device.<br/>
     * (we use a set for uniqueness of the registers)
     */
    private List<OfflineRegister> registers = new ArrayList<>();

    public ReadRegistersCommandImpl(final CompositeComCommand commandOwner, final CommandRoot commandRoot) {
        super(commandRoot);
        this.commandOwner = commandOwner;
    }

    @Override
    public void addRegisters (final List<OfflineRegister> registersToCollect) {
        if (registersToCollect != null) {
            for (OfflineRegister offlineRegister : registersToCollect) {
                if (canWeAddIt(offlineRegister)) {
                    this.registers.add(offlineRegister);
                }
            }
        }
    }

    /**
     * Check whether the {@link #registers} already contain this {@link OfflineRegister}
     *
     * @param offlineRegister the register to check
     * @return true if it does not exist yet, false otherwise
     */
    protected boolean canWeAddIt(final OfflineRegister offlineRegister) {
        boolean duplicate = false;
        for (OfflineRegister register : this.registers) {
            if (register.getSerialNumber().equals(offlineRegister.getSerialNumber())
                    && register.getRegisterId() == offlineRegister.getRegisterId()
                    && register.getObisCode().equals(offlineRegister.getObisCode())) {
                duplicate |= true;
            }
        }
        return !duplicate;
    }

    @Override
    public void doExecute (final DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        List<OfflineRegister> offlineRegisters = this.getOfflineRegisters();
        List<CollectedRegister> collectedRegisters = deviceProtocol.readRegisters(offlineRegisters);
        this.commandOwner.addListOfCollectedDataItems(collectedRegisters);
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.registers.isEmpty()) {
            builder.addLabel("No registers to read");
        }
        else {
            PropertyDescriptionBuilder registersToReadBuilder = builder.addListProperty("registersToRead");
            for (OfflineRegister offlineRegister : this.registers) {
                registersToReadBuilder = registersToReadBuilder.append(offlineRegister.getObisCode()).next();
            }
        }
    }

    public CompositeComCommand getCommandOwner() {
        return commandOwner;
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.READ_REGISTERS_COMMAND;
    }

    protected List<OfflineRegister> getOfflineRegisters () {
        return this.registers;
    }

}