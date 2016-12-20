package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedDeviceTopologyDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.time.Instant;

/**
 * An implementation of a CollectedMessage with additional CollectedTopology
 * <p>
 * Copyrights EnergyICT
 * Date: 1/5/15
 * Time: 1:59 PM
 */
public class DeviceProtocolMessageWithCollectedTopology extends CollectedDeviceData implements CollectedMessage {

    private final MessageIdentifier messageIdentifier;
    private final CollectedTopology collectedTopology;
    private Instant sentDate;
    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;
    private ComTaskExecution comTaskExecution;

    public DeviceProtocolMessageWithCollectedTopology(MessageIdentifier messageIdentifier, CollectedTopology collectedTopology) {
        this.messageIdentifier = messageIdentifier;
        this.collectedTopology = collectedTopology;
    }

    @Override
    public MessageIdentifier getMessageIdentifier() {
        return messageIdentifier;
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

    @Override
    public void setNewDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus) {
        this.deviceMessageStatus = deviceMessageStatus;
    }

    @Override
    public String getDeviceProtocolInformation() {
        return deviceProtocolInformation;
    }

    @Override
    public void setDeviceProtocolInformation(String deviceProtocolInformation) {
        this.deviceProtocolInformation = deviceProtocolInformation;
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {
        comTaskExecution = (ComTaskExecution) configuration;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedDeviceTopologyDeviceCommand(this.collectedTopology, comTaskExecution, meterDataStoreCommand, serviceProvider);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToSendMessages();
    }
}
