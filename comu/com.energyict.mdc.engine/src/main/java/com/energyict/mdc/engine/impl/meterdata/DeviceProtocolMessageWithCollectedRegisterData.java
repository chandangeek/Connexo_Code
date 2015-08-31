package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedRegisterListDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.util.List;

/**
 * An implementation of the CollectedMessage interface,
 * containing additional {@link CollectedRegister} data.
 *
 * @author sva
 * @since 17/06/13 - 11:23
 */
public class DeviceProtocolMessageWithCollectedRegisterData extends CollectedDeviceData implements CollectedMessage, CollectedRegisterList {

    private final MessageIdentifier deviceMessageIdentifier;
    private final DeviceIdentifier<?> deviceIdentifier;
    private List<CollectedRegister> collectedRegisters;

    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;

    public DeviceProtocolMessageWithCollectedRegisterData (DeviceIdentifier<?> deviceIdentifier, MessageIdentifier deviceMessageIdentifier, List<CollectedRegister> collectedRegisters) {
        this.deviceIdentifier = deviceIdentifier;
        this.deviceMessageIdentifier = deviceMessageIdentifier;
        this.collectedRegisters = collectedRegisters;
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToSendMessages();
    }

    @Override
    public MessageIdentifier getMessageIdentifier () {
        return this.deviceMessageIdentifier;
    }


    @Override
    public DeviceMessageStatus getNewDeviceMessageStatus () {
        return this.deviceMessageStatus;
    }

    public void setNewDeviceMessageStatus (DeviceMessageStatus deviceMessageStatus) {
        this.deviceMessageStatus = deviceMessageStatus;
    }

    @Override
    public String getDeviceProtocolInformation () {
        return this.deviceProtocolInformation;
    }

    @Override
    public void setDeviceProtocolInformation (String deviceProtocolInformation) {
        this.deviceProtocolInformation = deviceProtocolInformation;
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {

    }

    @Override
    public void injectComTaskExecution(ComTaskExecution comTaskExecution) {
        super.injectComTaskExecution(comTaskExecution);
        this.getCollectedRegisters()
                .stream()
                .filter(collectedRegister -> collectedRegister instanceof ServerCollectedData)
                .map(ServerCollectedData.class::cast)
                .forEach(collectedData -> collectedData.injectComTaskExecution(comTaskExecution));
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedRegisterListDeviceCommand(this, this.getComTaskExecution(), meterDataStoreCommand, serviceProvider);
    }

    @Override
    public void addCollectedRegister (CollectedRegister collectedRegister) {
        collectedRegisters.add(collectedRegister);
    }

    @Override
    public List<CollectedRegister> getCollectedRegisters () {
        return collectedRegisters;
    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier () {
        return deviceIdentifier;
    }
}
