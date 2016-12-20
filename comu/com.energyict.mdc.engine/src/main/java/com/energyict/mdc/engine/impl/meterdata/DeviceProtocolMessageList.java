package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.CollectedMessageListDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 21/03/13
 * Time: 16:34
 */
public class DeviceProtocolMessageList extends CompositeCollectedData<CollectedMessage> implements CollectedMessageList {

    private final List<OfflineDeviceMessage> offlineDeviceMessages;

    public DeviceProtocolMessageList(List<OfflineDeviceMessage> offlineDeviceMessages) {
        this.offlineDeviceMessages = offlineDeviceMessages;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToSendMessages();
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedMessageListDeviceCommand(this, this.offlineDeviceMessages, this.getComTaskExecution(), meterDataStoreCommand, serviceProvider);
    }

    @Override
    public void addCollectedMessage(CollectedMessage collectedMessage) {
        this.add(collectedMessage);
    }

    @Override
    public void addCollectedMessages(CollectedMessageList collectedMessages) {
        collectedMessages.getCollectedMessages().stream().forEach(this::add);
    }

    @Override
    public List<CollectedMessage> getCollectedMessages() {
        return this.getElements();
    }

    public List<CollectedMessage> getCollectedMessages(MessageIdentifier messageIdentifier) {
        return this.getCollectedMessages()
                .stream()
                .filter(x -> x.getMessageIdentifier().equals(messageIdentifier))
                .collect(Collectors.toList());
    }

}