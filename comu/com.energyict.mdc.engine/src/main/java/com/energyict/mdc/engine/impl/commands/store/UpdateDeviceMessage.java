/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.UpdateDeviceMessageEvent;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageAcknowledgement;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 3/07/13 - 16:17
 */
public class UpdateDeviceMessage extends DeviceCommandImpl<UpdateDeviceMessageEvent> {

    public final static String DESCRIPTION_TITLE = "Update device message";

    private MessageIdentifier messageIdentifier;
    private DeviceMessageStatus deviceMessageStatus;
    private Instant sentDate;
    private String protocolInfo;

    public UpdateDeviceMessage(DeviceProtocolMessageAcknowledgement messageAcknowledgement, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.messageIdentifier = messageAcknowledgement.getMessageIdentifier();
        this.deviceMessageStatus = messageAcknowledgement.getDeviceMessageStatus();
        this.sentDate = messageAcknowledgement.getSentDate();
        this.protocolInfo = messageAcknowledgement.getProtocolInfo();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        Optional<OfflineDeviceMessage> offlineDeviceMessage = comServerDAO.findOfflineDeviceMessage(this.messageIdentifier);
        if (offlineDeviceMessage.isPresent()) {
            comServerDAO.updateDeviceMessageInformation(this.messageIdentifier, this.deviceMessageStatus, this.sentDate, this.protocolInfo);
        } else {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.UNKNOWN_DEVICE_MESSAGE,
                            this.messageIdentifier)
            );
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("messageIdentifier").append(this.messageIdentifier);
            builder.addProperty("message status").append(this.deviceMessageStatus);
        }
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
            builder.addProperty("sent date").append(this.sentDate);
            builder.addProperty("protocolInfo").append(this.protocolInfo);
        }
    }

    public MessageIdentifier getMessageIdentifier() {
        return messageIdentifier;
    }

    protected Optional<UpdateDeviceMessageEvent> newEvent(List<Issue> issues) {
        UpdateDeviceMessageEvent event = new UpdateDeviceMessageEvent(new ComServerEventServiceProvider(), this.messageIdentifier, this.deviceMessageStatus, protocolInfo);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}