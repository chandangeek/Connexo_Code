package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedLogBookDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.time.Instant;

/**
 * An implementation of the {@link CollectedMessage} interface,
 * containing additional {@link CollectedLogBook} data.
 *
 * @author sva
 * @since 17/06/13 - 11:23
 */
public class DeviceProtocolMessageWithCollectedLogbookData extends CollectedDeviceData implements CollectedMessage {

    private final MessageIdentifier deviceMessageIdentifier;
    private final CollectedLogBook collectedLogBook;

    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;
    private DataCollectionConfiguration configuration;
    private Instant sentDate;

    public DeviceProtocolMessageWithCollectedLogbookData(MessageIdentifier deviceMessageIdentifier, CollectedLogBook collectedLogBook) {
        this.deviceMessageIdentifier = deviceMessageIdentifier;
        this.collectedLogBook = collectedLogBook;
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
    public void injectComTaskExecution(ComTaskExecution comTaskExecution) {
        super.injectComTaskExecution(comTaskExecution);
        if (collectedLogBook != null && ServerCollectedData.class.isAssignableFrom(collectedLogBook.getClass())) {      //Also add it to the nested collected log books!
            ((ServerCollectedData) collectedLogBook).injectComTaskExecution(comTaskExecution);
        }
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedLogBookDeviceCommand(collectedLogBook, getComTaskExecution(), meterDataStoreCommand);
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