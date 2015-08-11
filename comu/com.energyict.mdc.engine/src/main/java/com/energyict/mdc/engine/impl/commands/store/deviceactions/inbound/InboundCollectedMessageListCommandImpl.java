package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.MessagesCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageAcknowledgement;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.MessagesTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11/17/14
 * Time: 3:33 PM
 */
public class InboundCollectedMessageListCommandImpl extends MessagesCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedMessageListCommandImpl(MessagesTask messagesTask, OfflineDevice device, CommandRoot commandRoot, List<ServerCollectedData> collectedData, ComTaskExecution comTaskExecution) {
        super(messagesTask, device, commandRoot, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        collectedData.stream().filter(dataItem -> dataItem instanceof CollectedMessage
                || dataItem instanceof CollectedMessageList
                || dataItem instanceof CollectedMessageAcknowledgement
                || dataItem instanceof DeviceIpAddress).forEach(this::addCollectedDataItem);
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
