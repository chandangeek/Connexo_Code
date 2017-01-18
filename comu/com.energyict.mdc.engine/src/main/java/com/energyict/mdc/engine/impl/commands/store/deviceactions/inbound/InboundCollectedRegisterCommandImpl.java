package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.RegisterCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/4/13
 * Time: 8:47 AM
 */
public class InboundCollectedRegisterCommandImpl extends RegisterCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedRegisterCommandImpl(GroupedDeviceCommand groupedDeviceCommand, RegistersTask registersTask, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData) {
        super(groupedDeviceCommand, registersTask, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public String getDescriptionTitle() {
        return "Collect inbound register data";
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        for (ServerCollectedData dataItem : collectedData) {
            if (dataItem instanceof CollectedRegister
                    | dataItem instanceof CollectedRegisterList
                    | dataItem instanceof CollectedDeviceInfo) {
                this.addCollectedDataItem(dataItem);
            }
        }
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (getListOfCollectedRegisters().isEmpty()) {
            builder.addLabel("No registers collected");
        } else {
            if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
                PropertyDescriptionBuilder registersToReadBuilder = builder.addListProperty("registers");
                for (CollectedRegister register : getListOfCollectedRegisters()) {
                    registersToReadBuilder.append("(");
                    registersToReadBuilder.append(register.getRegisterIdentifier().getRegisterObisCode());
                    if (register.getCollectedQuantity() != null) {
                        registersToReadBuilder.append(" - ");
                        registersToReadBuilder.append(register.getCollectedQuantity());
                    }
                    if (register.getText() != null && !register.getText().isEmpty()) {
                        registersToReadBuilder.append(" - ");
                        registersToReadBuilder.append(register.getText());
                    }
                    registersToReadBuilder.append(")");
                    registersToReadBuilder.next();
                }
            } else {
                builder.addProperty("nrOfRegistersCollected").append(getListOfCollectedRegisters().size());
            }
        }
    }

    private List<CollectedRegister> getListOfCollectedRegisters() {
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        for (CollectedData data : getCollectedData()) {
            if (data instanceof CollectedRegisterList) {
                collectedRegisters.addAll(((CollectedRegisterList) data).getCollectedRegisters());
            } else if (data instanceof CollectedRegister) {
                collectedRegisters.add((CollectedRegister) data);
            }
        }
        return collectedRegisters;
    }
}