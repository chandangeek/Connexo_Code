package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceMessage;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageAcknowledgement;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

/**
 * @author sva
 * @since 3/07/13 - 16:08
 */
public class DeviceProtocolMessageAcknowledgement extends CollectedDeviceData implements CollectedMessageAcknowledgement {

    private final MessageIdentifier messageIdentifier;
    private DeviceMessageStatus deviceMessageStatus;
    private String protocolInfo;

    public DeviceProtocolMessageAcknowledgement(MessageIdentifier messageIdentifier) {
        this.messageIdentifier = messageIdentifier;
    }

    @Override
    public MessageIdentifier getMessageIdentifier() {
        return this.messageIdentifier;
    }

    @Override
    public DeviceMessageStatus getDeviceMessageStatus() {
        return this.deviceMessageStatus;
    }

    @Override
    public void setDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus) {
        this.deviceMessageStatus = deviceMessageStatus;
    }

    @Override
    public String getProtocolInfo() {
        return this.protocolInfo;
    }

    @Override
    public void setProtocolInfo(String protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService, MeterDataStoreCommand meterDataStoreCommand) {
        return new UpdateDeviceMessage(this);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToSendMessages();
    }
}
