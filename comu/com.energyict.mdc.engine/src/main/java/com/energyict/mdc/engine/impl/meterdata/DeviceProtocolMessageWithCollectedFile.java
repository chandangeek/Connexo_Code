package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.StoreConfigurationUserFile;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedConfigurationInformation;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.time.Instant;


public class DeviceProtocolMessageWithCollectedFile extends CollectedDeviceData implements CollectedMessage, CollectedConfigurationInformation {
    private final MessageIdentifier deviceMessageIdentifier;
    private final String fileName;
    private final String fileExtension;
    private final byte[] contents;
    private final DeviceIdentifier deviceIdentifier;

    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;
    private Instant sentDate;
    private DataCollectionConfiguration configuration;

    public DeviceProtocolMessageWithCollectedFile(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String fileName, String fileExtension, byte[] contents) {
        this.deviceIdentifier = deviceIdentifier;
        this.deviceMessageIdentifier = messageIdentifier;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.contents = contents;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
        return comTask.isConfiguredToSendMessages();
    }

    @Override
    public MessageIdentifier getMessageIdentifier() {
        return this.deviceMessageIdentifier;
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
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new StoreConfigurationUserFile(this, this.getComTaskExecution(), serviceProvider);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public byte[] getContents() {
        return contents;
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Instant getSentDate() {
        return this.sentDate;
    }

    @Override
    public void setSentDate(Instant sentDate) {
        this.sentDate = sentDate;
    }

}