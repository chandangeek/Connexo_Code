package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.CollectedMessageListDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;

import java.util.List;

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
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new CollectedMessageListDeviceCommand(this, offlineDeviceMessages, issueService);
    }

    @Override
    public void addCollectedMessages(CollectedMessage collectedMessage) {
        this.add(collectedMessage);
    }

    @Override
    public List<CollectedMessage> getCollectedMessages() {
        return this.getElements();
    }

}