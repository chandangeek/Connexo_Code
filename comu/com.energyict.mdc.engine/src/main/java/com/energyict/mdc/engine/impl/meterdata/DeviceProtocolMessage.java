package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 21/03/13
 * Time: 16:27
 */
public class DeviceProtocolMessage extends CollectedDeviceData implements CollectedMessage {

    private final MessageIdentifier deviceMessageIdentifier;

    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;
    private Instant sentDate;

    public DeviceProtocolMessage(MessageIdentifier deviceMessageIdentifier) {
        this.deviceMessageIdentifier = deviceMessageIdentifier;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToSendMessages();
    }

    @Override
    public MessageIdentifier getMessageIdentifier() {
        return this.deviceMessageIdentifier;
    }

    @Override
    public Instant getSentDate() {
        return sentDate;
    }

    public void setSentDate(Instant sentDate) {
        this.sentDate = sentDate;
    }

    @Override
    public DeviceMessageStatus getNewDeviceMessageStatus() {
        return this.deviceMessageStatus;
    }

    public void setNewDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus) {
        this.deviceMessageStatus = deviceMessageStatus;
    }

    @Override
    public String getDeviceProtocolInformation() {
        return this.deviceProtocolInformation;
    }

    @Override
    public void setDeviceProtocolInformation(String deviceProtocolInformation) {
        this.deviceProtocolInformation = deviceProtocolInformation;
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {

    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new NoopDeviceCommand();
    }
}
