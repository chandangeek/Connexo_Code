package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageAcknowledgement;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

/**
 * @author sva
 * @since 3/07/13 - 16:17
 */
public class UpdateDeviceMessage extends DeviceCommandImpl {

    private MessageIdentifier messageIdentifier;
    private DeviceMessageStatus deviceMessageStatus;
    private String protocolInfo;

    public UpdateDeviceMessage(DeviceProtocolMessageAcknowledgement messageAcknowledgement, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.messageIdentifier = messageAcknowledgement.getMessageIdentifier();
        this.deviceMessageStatus = messageAcknowledgement.getDeviceMessageStatus();
        this.protocolInfo = messageAcknowledgement.getProtocolInfo();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateDeviceMessageInformation(this.messageIdentifier, this.deviceMessageStatus, this.protocolInfo);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("messageIdentifier").append(this.messageIdentifier);
            builder.addProperty("message status").append(this.deviceMessageStatus);
        }
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
            builder.addProperty("protocolInfo").append(this.protocolInfo);
        }
    }

    public MessageIdentifier getMessageIdentifier() {
        return messageIdentifier;
    }

    @Override
    public String getDescriptionTitle() {
        return "Update device message";
    }

}