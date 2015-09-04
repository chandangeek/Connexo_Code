package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.tasks.RegistersTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for the {@link RegisterCommand}
 *
 * @author gna
 * @since 12/06/12 - 11:00
 */
public class RegisterCommandImpl extends CompositeComCommandImpl implements RegisterCommand {

    /**
     * The task used for modeling this command
     */
    private final RegistersTask registersTask;

    /**
     * A List containing all the {@link CollectedRegister} which is collected during the execution of this {@link RegisterCommand}
     */
    private CollectedRegisterList deviceRegisterList;

    /**
     * A List containing all the {@link CollectedData} - which is not an instance of CollectedRegister - which is collected during the execution of this {@link RegisterCommand}
     */
    private List<CollectedData> collectedDataList = new ArrayList<>();

    public RegisterCommandImpl(RegistersTask registersTask, OfflineDevice device, CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        if (registersTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "registersTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.registersTask = registersTask;

        List<OfflineRegister> registers;
        if (!this.registersTask.getRegisterGroups().isEmpty()){
            registers = device.getRegistersForRegisterGroupAndMRID(this.getRegisterGroupIds(), comTaskExecution.getDevice().getmRID());
        } else {
            registers = device.getAllRegistersForMRID(comTaskExecution.getDevice().getmRID());
        }
        ReadRegistersCommand readRegistersCommand = getCommandRoot().getReadRegistersCommand(this, comTaskExecution);
        readRegistersCommand.addRegisters(registers);
        deviceRegisterList = new DeviceRegisterList(device.getDeviceIdentifier());
    }

    private List<Long> getRegisterGroupIds () {
        List<RegisterGroup> registerGroups = this.registersTask.getRegisterGroups();
        List<Long> registerGroupIds = new ArrayList<>(registerGroups.size());
        for (RegisterGroup registerGroup : registerGroups) {
            registerGroupIds.add(registerGroup.getId());
        }
        return registerGroupIds;
    }

    @Override
    public void addListOfCollectedDataItems(List<? extends CollectedData> collectedDataList) {
        for (CollectedData collectedData : collectedDataList) {
            addCollectedDataItem(collectedData);
        }
    }

    @Override
    public void addCollectedDataItem(CollectedData collectedData) {
        if (!(collectedData instanceof CollectedRegister)) {
            this.collectedDataList.add(collectedData);   // If not of type CollectedRegister, then add it directly to the collectedDataList
            return;
        }

        CollectedRegister collectedRegister = (CollectedRegister) collectedData;
        this.deviceRegisterList.addCollectedRegister(collectedRegister);
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> dataList = new ArrayList<>();
        dataList.addAll(collectedDataList);
        if (!deviceRegisterList.getCollectedRegisters().isEmpty()) {
            dataList.add(deviceRegisterList);
        }
        return dataList;
    }

    /**
     * The RegistersTask which is used for modeling this command
     *
     * @return the RegistersTask
     */
    @Override
    public RegistersTask getRegistersTask() {
        return this.registersTask;
    }

    /**
     * @return the ComCommandTypes  of this command
     */
    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.REGISTERS_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed register protocol task";
    }

}