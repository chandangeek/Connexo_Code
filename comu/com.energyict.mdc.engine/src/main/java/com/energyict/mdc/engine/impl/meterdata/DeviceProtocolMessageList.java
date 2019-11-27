/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.impl.commands.store.CollectedMessageListDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceProtocolMessageList extends CompositeCollectedData<CollectedMessage> implements CollectedMessageList {

    private final List<OfflineDeviceMessage> offlineDeviceMessages;
    private final DeviceMessageService deviceMessageService;

    public DeviceProtocolMessageList(List<OfflineDeviceMessage> offlineDeviceMessages, DeviceMessageService deviceMessageService) {
        this.offlineDeviceMessages = offlineDeviceMessages;
        this.deviceMessageService = deviceMessageService;
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
                .filter(x -> compareDeviceMessages(messageIdentifier, x.getMessageIdentifier()))
                .collect(Collectors.toList());
    }

    /**
     * Resolve the given identifiers to compare the messages.
     */
    private boolean compareDeviceMessages(MessageIdentifier messageIdentifier1, MessageIdentifier messageIdentifier2) {
        return messageIdentifier1.equals(messageIdentifier2);
    }
}