/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.FirmwareManagementCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Straightforward implementation of a FirmwareUpgrade task
 */
public class FirmwareManagementCommandImpl extends SimpleComCommand implements FirmwareManagementCommand {

    private final FirmwareManagementTask firmwareManagementTask;
    private final ComTaskExecution comTaskExecution;
    private final OfflineDevice device;

    /**
     * Should contain the actual FirmwareUpgrade message, including the activate if necessary
     */
    private List<OfflineDeviceMessage> firmwareDeviceMessages;
    private List<CollectedMessageList> messagesCollectedData = new ArrayList<>();

    public FirmwareManagementCommandImpl(GroupedDeviceCommand groupedDeviceCommand, FirmwareManagementTask firmwareManagementTask, ComTaskExecution comTaskExecution, OfflineDevice device) {
        super(groupedDeviceCommand);
        this.firmwareManagementTask = firmwareManagementTask;
        this.comTaskExecution = comTaskExecution;
        this.device = device;
        updateFirmwareMessages();
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        messagesCollectedData.add(deviceProtocol.updateSentMessages(Collections.emptyList())); // is required in order for old protocols to delegate to the protocol itself ...
        if (this.firmwareDeviceMessages.isEmpty()) {
            addIssue(getCommandRoot().getServiceProvider().issueService().newProblem(this, MessageSeeds.NOT_EXECUTED_DUE_TO_MISSING_FIRMWARE_MESSAGES), CompletionCode.NotExecuted);
        }
        messagesCollectedData.add(deviceProtocol.executePendingMessages(this.firmwareDeviceMessages));
        messagesCollectedData.stream().forEach(collectedMessageList -> collectedMessageList.getCollectedMessages().forEach(collectedMessage -> collectedMessage.setDataCollectionConfiguration(comTaskExecution)));
        addListOfCollectedDataItems(messagesCollectedData);
    }

    private void updateFirmwareMessages() {
        this.firmwareDeviceMessages = this.device.getAllPendingDeviceMessages().stream()
                .filter(this::isValidFirmwareCommand)
                .filter(this::isTheMessageForThisComTask).collect(Collectors.toList());
    }

    private boolean isValidFirmwareCommand(OfflineDeviceMessage offlineDeviceMessage) {
        return offlineDeviceMessage.isFirmwareMessage();
    }

    private boolean isTheMessageForThisComTask(OfflineDeviceMessage offlineDeviceMessage) {
        return offlineDeviceMessage.getDeviceId() == comTaskExecution.getDevice().getId();
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed firmware upgrade";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.FIRMWARE_COMMAND;
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);

        if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            this.appendMessages(builder);
        } else {
            builder.addProperty("nrOfFirmwareMessages").append(this.firmwareDeviceMessages.size());
        }
        this.appendMessagesExecutionStatus(builder);
    }

    private void appendMessages(DescriptionBuilder builder) {
        this.appendMessagesInfo(builder, this.firmwareDeviceMessages, "There are no pending messages", "pendingMessages");

    }

    private void appendMessagesInfo(DescriptionBuilder builder, List<OfflineDeviceMessage> messages, String noMessagesDescription, String description) {
        if (messages.isEmpty()) {
            builder.addLabel(noMessagesDescription);
        }
        else {
            PropertyDescriptionBuilder descriptionBuilder = builder.addListProperty(description);
            for (OfflineDeviceMessage message : messages) {
                descriptionBuilder.append("(");
                descriptionBuilder.append(message.getDeviceMessageId());
                descriptionBuilder.append(", ");
                descriptionBuilder.append(message.getSpecification().getCategory().getName());
                descriptionBuilder.append(" - ");
                descriptionBuilder.append(message.getSpecification().getName());
                descriptionBuilder.append(")");
                descriptionBuilder.next();
            }
        }
    }

    private void appendMessagesExecutionStatus (DescriptionBuilder sb) {
        this.appendMessagesExecutionStatus(sb, this.messagesCollectedData, "Updated messageStatus");
    }

    private void appendMessagesExecutionStatus(DescriptionBuilder builder, List<CollectedMessageList> collectedMessagesList, String description) {
        for (CollectedMessageList collectedMessages : collectedMessagesList) {
            if (!collectedMessages.getCollectedMessages().isEmpty()) {
                PropertyDescriptionBuilder descriptionBuilder = builder.addListProperty(description);
                for (CollectedMessage message : collectedMessages.getCollectedMessages()) {
                    descriptionBuilder.append("(");
                    descriptionBuilder.append(message.getMessageIdentifier());
                    descriptionBuilder.append(", ");
                    descriptionBuilder.append(message.getNewDeviceMessageStatus());
                    descriptionBuilder.append(")");
                    descriptionBuilder.next();
                }
            }
        }
    }
}
