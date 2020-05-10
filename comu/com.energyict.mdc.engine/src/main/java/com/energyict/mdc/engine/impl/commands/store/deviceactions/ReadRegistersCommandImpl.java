/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.engine.impl.meterdata.DeviceTextRegister;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation for a {@link ReadRegistersCommand}.
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
    private List<CollectedRegister> collectedRegisters = new ArrayList<>();

    public ReadRegistersCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final CompositeComCommand commandOwner) {
        super(groupedDeviceCommand);
        this.commandOwner = commandOwner;
    }

    @Override
    public void addRegisters(final List<OfflineRegister> registersToCollect) {
        if (registersToCollect != null) {
            registersToCollect
                    .stream()
                    .filter(this::canWeAddIt)
                    .forEach(this.registers::add);
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
        return register2.getDeviceMRID().equals(register1.getDeviceMRID())
                && register2.getRegisterId() == register1.getRegisterId()
                && register2.getObisCode().equals(register1.getObisCode());
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        verifyObisCodeRequiresSerialNumber();
        collectedRegisters = deviceProtocol.readRegisters(this.registers);
        this.commandOwner.addListOfCollectedDataItems(convertToTextRegistersIfRequired(this.registers, collectedRegisters));
    }

    /**
     * An obis code that contains an x (any channel) for the B-field requires the serial number to be filled in.
     * If it's not filled in, that specific register cannot (and will not) be read out. A proper issue will be logged.
     */
    private void verifyObisCodeRequiresSerialNumber() {
        List<OfflineRegister> registersToRemove = new ArrayList<>();
        for (OfflineRegister offlineRegister : this.registers) {
            if (offlineRegister.getObisCode().anyChannel() && (isEmpty(offlineRegister.getSerialNumber()))) {
                Problem issue = getIssueService().newProblem(
                        offlineRegister.getObisCode(),
                        MessageSeeds.ANY_CHANNEL_OBIS_CODE_REQUIRES_SERIAL_NUMBER,
                        offlineRegister.getObisCode()
                );
                registersToRemove.add(offlineRegister);
                createAndAddFailedCollectedRegister(offlineRegister, issue, ResultType.ConfigurationError);
            }
        }

        registersToRemove.forEach(registerToRemove -> this.registers.remove(registerToRemove));
    }

    private void createAndAddFailedCollectedRegister(OfflineRegister offlineRegister, Issue issues, ResultType resultType) {
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(offlineRegister.getRegisterIdentifier());
        collectedRegister.setFailureInformation(resultType, issues);
        addCollectedDataItem(collectedRegister);
    }

    public CompositeComCommand getCommandOwner() {
        return commandOwner;
    }

    private List<CollectedData> convertToTextRegistersIfRequired(List<OfflineRegister> offlineRegisters, List<CollectedRegister> collectedRegisters) {
        return collectedRegisters.stream().
                flatMap(toCollectedRegister(offlineRegisters)).
                collect(Collectors.toList());
    }

    private Function<CollectedRegister, Stream<CollectedRegister>> toCollectedRegister(List<OfflineRegister> offlineRegisters) {
        return collectedRegister ->
                offlineRegisters.stream().
                        filter(offlineRegister -> offlineRegister.getObisCode().equals(collectedRegister.getRegisterIdentifier().getRegisterObisCode())).
                        map(offlineRegister -> this.toCollectedRegister(offlineRegister, collectedRegister));
    }

    private CollectedRegister toCollectedRegister(OfflineRegister offlineRegister, CollectedRegister collectedRegister) {
        CollectedRegister register;
        if (!offlineRegister.isText()) {
            register = new DefaultDeviceRegister(collectedRegister.getRegisterIdentifier());
            register.setCollectedTimeStamps(collectedRegister.getReadTime(), collectedRegister.getFromTime(), collectedRegister.getToTime(), collectedRegister.getEventTime());
            register.setCollectedData(collectedRegister.getCollectedQuantity());
        } else if (collectedRegister.getCollectedQuantity() != null) {
            register = new DeviceTextRegister(collectedRegister.getRegisterIdentifier());
            register.setCollectedTimeStamps(collectedRegister.getReadTime(), collectedRegister.getFromTime(), collectedRegister.getToTime());
            register.setCollectedData(collectedRegister.getCollectedQuantity().toString());
        } else {
            register = new DeviceTextRegister(collectedRegister.getRegisterIdentifier());
            register.setCollectedTimeStamps(collectedRegister.getReadTime(), collectedRegister.getFromTime(), collectedRegister.getToTime());
            register.setCollectedData(collectedRegister.getText());
        }

        for (Issue issue : collectedRegister.getIssues()) {
            register.setFailureInformation(collectedRegister.getResultType(), issue);
        }

        return register;
    }

    @Override
    public String getDescriptionTitle() {
        return "Read out the device registers";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.registers.isEmpty()) {
            builder.addLabel("No registers to read");
        } else {
            if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
                PropertyDescriptionBuilder registersToReadBuilder = builder.addListProperty("registers");
                for (OfflineRegister offlineRegister : this.registers) {
                    registersToReadBuilder.append("(");
                    registersToReadBuilder.append(offlineRegister.getObisCode());
                    CollectedRegister collectedRegister = getCollectedRegisterForRegister(offlineRegister);
                    if (collectedRegister != null) {
                        if (collectedRegister.getCollectedQuantity() != null) {
                            registersToReadBuilder.append(" - ");
                            registersToReadBuilder.append(collectedRegister.getCollectedQuantity());
                        }
                        if (collectedRegister.getText() != null && !collectedRegister.getText().isEmpty()) {
                            registersToReadBuilder.append(" - ");
                            registersToReadBuilder.append(getPrintableCollectedRegisterText(collectedRegister));
                        }
                    }
                    registersToReadBuilder.append(")");
                    registersToReadBuilder.next();
                }
            } else {
                builder.addProperty("nrOfRegistersToRead").append(this.registers.size());
            }
        }
    }

    private String getPrintableCollectedRegisterText(CollectedRegister collectedRegister) {
        return collectedRegister.getText().replaceAll("\\p{Cntrl}", ".");
    }

    private CollectedRegister getCollectedRegisterForRegister(OfflineRegister register) {
        for (CollectedRegister collectedRegister : collectedRegisters) {
            ObisCode registerObisCode = collectedRegister.getRegisterIdentifier().getRegisterObisCode();
            if (registerObisCode != null && registerObisCode.equals(register.getObisCode())) {
                return collectedRegister;
            }
        }
        return null;
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.READ_REGISTERS_COMMAND;
    }

    List<OfflineRegister> getOfflineRegisters() {
        return this.registers;
    }

}