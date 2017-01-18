package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.offline.OfflineRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for the {@link RegisterCommand}
 *
 * @author gna
 * @since 12/06/12 - 11:00
 */
public class RegisterCommandImpl extends CompositeComCommandImpl implements RegisterCommand {

    /**
     * A List containing all the {@link CollectedRegister} which is collected during the execution of this {@link RegisterCommand}
     */
    private CollectedRegisterList deviceRegisterList;

    /**
     * A List containing all the {@link CollectedData} - which is not an instance of CollectedRegister - which is collected during the execution of this {@link RegisterCommand}
     */
    private List<CollectedData> collectedDataList = new ArrayList<>();

    private ReadRegistersCommand readRegistersCommand;

    public RegisterCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final RegistersTask registersTask, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        if (groupedDeviceCommand == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "groupedDeviceCommand", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (groupedDeviceCommand.getOfflineDevice() == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "offlineDevice", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (registersTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "registersTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (comTaskExecution == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "comTaskExecution", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }

        readRegistersCommand = getGroupedDeviceCommand().getReadRegistersCommand(this, comTaskExecution);
        readRegistersCommand.addRegisters(createOfflineRegisters(registersTask, groupedDeviceCommand.getOfflineDevice(), comTaskExecution));
        deviceRegisterList = new DeviceRegisterList(groupedDeviceCommand.getOfflineDevice().getDeviceIdentifier());
    }

    private List<OfflineRegister> createOfflineRegisters(RegistersTask registersTask, OfflineDevice offlineDevice, ComTaskExecution comTaskExecution) {
        List<OfflineRegister> registers = new ArrayList<>();
        if (registersTask.getRegisterGroups().size() > 0) {
            //Only add the registers of the master or the slave, not both
            List<Long> ids = registersTask.getRegisterGroups().stream().map(RegisterGroup::getId).collect(Collectors.toList());

            List<OfflineRegister> filteredRegisters = offlineDevice
                    .getAllOfflineRegisters()
                    .stream()
                    .filter(register -> register.inAtLeastOneGroup(ids) && register.getDeviceMRID().equals(comTaskExecution.getDevice().getmRID()))
                    .collect(Collectors.toList());

            registers.addAll(filteredRegisters);
        } else {
            List<OfflineRegister> allRegisters = offlineDevice.getAllOfflineRegisters();
            //Only add the registers of the master or the slave, not both
            registers.addAll(allRegisters.stream().filter(register -> comTaskExecution.getDevice().getmRID().equals(register.getDeviceMRID())).collect(Collectors.toList()));
        }
        return registers;
    }

    @Override
    public void addAdditionalRegisterGroups(RegistersTask registersTask, OfflineDevice offlineDevice, ComTaskExecution comTaskExecution) {
        readRegistersCommand.addRegisters(createOfflineRegisters(registersTask, offlineDevice, comTaskExecution));
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

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.REGISTERS_COMMAND;
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed register protocol task";
    }

}