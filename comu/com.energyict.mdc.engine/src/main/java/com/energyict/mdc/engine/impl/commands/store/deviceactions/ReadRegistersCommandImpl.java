package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.engine.impl.meterdata.DeviceTextRegister;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public void addRegisters(final List<OfflineRegister> registersToCollect) {
        if (registersToCollect != null) {
            this.registers.addAll(registersToCollect.stream().filter(this::canWeAddIt).collect(Collectors.toList()));
        }
    }

    /**
     * Check whether the {@link #registers} already contain this {@link OfflineRegister}
     *
     * @param offlineRegister the register to check
     * @return true if it does not exist yet, false otherwise
     */
    private boolean canWeAddIt(final OfflineRegister offlineRegister) {
        for (OfflineRegister register : this.registers) {
            if (areDuplicates(offlineRegister, register)) {
                return false;
            }
        }
        return true;
    }

    private boolean areDuplicates(OfflineRegister register1, OfflineRegister register2) {
        return register2.getSerialNumber().equals(register1.getSerialNumber())
                && register2.getRegisterId() == register1.getRegisterId()
                && register2.getObisCode().equals(register1.getObisCode());
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        List<OfflineRegister> offlineRegisters = this.getOfflineRegisters();
        List<CollectedRegister> collectedRegisters = deviceProtocol.readRegisters(offlineRegisters);
        this.commandOwner.addListOfCollectedDataItems(convertToTextRegistersIfRequired(offlineRegisters, collectedRegisters));
    }

    private List<CollectedData> convertToTextRegistersIfRequired(List<OfflineRegister> offlineRegisters, List<CollectedRegister> collectedRegisters) {
        List<CollectedData> result = new ArrayList<>();
        for (CollectedRegister collectedRegister : collectedRegisters) {
            offlineRegisters.stream()
                    .filter(offlineRegister -> collectedRegister.getReadingType().equals(offlineRegister.getReadingType()))
                    .forEach(offlineRegister -> {
                        CollectedRegister register;
                        if (!offlineRegister.isText()) {
                            register = new DefaultDeviceRegister(collectedRegister.getRegisterIdentifier(), collectedRegister.getReadingType());
                            register.setCollectedTimeStamps(collectedRegister.getReadTime(), collectedRegister.getFromTime(), collectedRegister.getToTime(), collectedRegister.getEventTime());
                            register.setCollectedData(collectedRegister.getCollectedQuantity());
                        } else if (collectedRegister.getCollectedQuantity() != null) {
                            register = new DeviceTextRegister(collectedRegister.getRegisterIdentifier(), collectedRegister.getReadingType());
                            register.setCollectedTimeStamps(collectedRegister.getReadTime(), collectedRegister.getFromTime(), collectedRegister.getToTime());
                            register.setCollectedData(collectedRegister.getCollectedQuantity().toString());
                        } else {
                            register = new DeviceTextRegister(collectedRegister.getRegisterIdentifier(), collectedRegister.getReadingType());
                            register.setCollectedTimeStamps(collectedRegister.getReadTime(), collectedRegister.getFromTime(), collectedRegister.getToTime());
                            register.setCollectedData(collectedRegister.getText());
                        }
                        result.add(register);
                    });
        }
        return result;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.registers.isEmpty()) {
            builder.addLabel("No registers to read");
        } else {
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

    List<OfflineRegister> getOfflineRegisters() {
        return this.registers;
    }

}