/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.MessagesCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.upl.meterdata.*;

import java.util.ArrayList;
import java.util.List;

public class InboundCollectedMessageListCommandImpl extends MessagesCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedMessageListCommandImpl(GroupedDeviceCommand groupedDeviceCommand, MessagesTask messagesTask, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData) {
        super(groupedDeviceCommand, messagesTask, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        collectedData.stream().filter(dataItem -> dataItem instanceof CollectedMessage
                || dataItem instanceof CollectedMessageList
                || dataItem instanceof CollectedMessageAcknowledgement
                || dataItem instanceof DeviceIpAddress
                || dataItem instanceof CollectedDeviceCache)
                .forEach(this::addCollectedDataItem);
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }

    @Override
    public String getDescriptionTitle() {
        return "Collect inbound message data";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toSuperJournalMessageDescription(builder, serverLogLevel);
        this.appendMessagesExecutionStatus(builder, getListOfCollectedMessages());
    }

    private void appendMessagesExecutionStatus(DescriptionBuilder builder, List<CollectedMessage> collectedMessages) {
        if (getListOfCollectedMessages().isEmpty()) {
            builder.addLabel("No messages collected");
        } else {
            PropertyDescriptionBuilder descriptionBuilder = builder.addListProperty("Collected messages");
            for (CollectedMessage message : collectedMessages) {
                descriptionBuilder.append("(");
                descriptionBuilder.append(message.getMessageIdentifier());
                descriptionBuilder.append(", ");
                descriptionBuilder.append(message.getNewDeviceMessageStatus());
                descriptionBuilder.append(")");
                descriptionBuilder.next();
            }
        }
    }

    private List<CollectedMessage> getListOfCollectedMessages() {
        List<CollectedMessage> collectedMessages = new ArrayList<>();
        for (CollectedData data : getCollectedData()) {
            if (data instanceof CollectedMessageList) {
                collectedMessages.addAll(((CollectedMessageList) data).getCollectedMessages());
            } else if (data instanceof CollectedMessage) {
                collectedMessages.add((CollectedMessage) data);
            }
        }
        return collectedMessages;
    }
}
