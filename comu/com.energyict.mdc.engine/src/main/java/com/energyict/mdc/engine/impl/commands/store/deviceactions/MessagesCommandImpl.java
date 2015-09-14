package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.MessagesCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.tasks.MessagesTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link MessagesCommand}
 * Copyrights EnergyICT
 * Date: 11/17/14
 * Time: 2:39 PM
 */
public class MessagesCommandImpl extends SimpleComCommand implements MessagesCommand {

    /**
     * The task used for modeling this command
     */
    private final MessagesTask messagesTask;
    private final OfflineDevice device;
    private List<OfflineDeviceMessage> pendingMessages;
    private List<OfflineDeviceMessage> sentMessages;
    private List<Integer> allowedCategories;

    private List<CollectedMessageList> messagesCollectedData = new ArrayList<>(2);
    private ComTaskExecution comTaskExecution;

    public MessagesCommandImpl(final MessagesTask messagesTask, final OfflineDevice device, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        this.comTaskExecution = comTaskExecution;
        if (messagesTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "messagesTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (comTaskExecution == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "comTaskExecution", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.messagesTask = messagesTask;
        this.device = device;
        createAllowedCategoryPrimaryKeyList();
        updateMessageLists(comTaskExecution);
    }

    @Override
    public String getDescriptionTitle() {
        return "Handle all device messages";
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        toSuperJournalMessageDescription(builder, serverLogLevel);

        if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            this.appendPendingMessages(builder);
            this.appendSentMessages(builder);
        } else {
            builder.addProperty("nrOfPendingMessages").append(this.pendingMessages.size());
            builder.addProperty("nrOfMessagesFromPreviousSessions").append(this.sentMessages.size());
        }
        this.appendMessagesExecutionStatus(builder);
    }

    protected void toSuperJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
    }

    private void appendPendingMessages (DescriptionBuilder sb) {
        this.appendMessagesInfo(sb, this.pendingMessages, "There are no pending messages", "pendingMessages");
    }

    private void appendSentMessages (DescriptionBuilder sb) {
        this.appendMessagesInfo(sb, this.sentMessages, "There are no messages to update from previous sessions", "messagesFromPreviousSession");
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

    private void updateMessageLists(ComTaskExecution comTaskExecution) {
        this.pendingMessages = new ArrayList<>();
        this.sentMessages = new ArrayList<>();
        for (OfflineDeviceMessage offlineDeviceMessage : this.device.getAllPendingDeviceMessages()) {
            if (offlineDeviceMessage.getDeviceId() == comTaskExecution.getDevice().getId()) {    //Only add the messages of the master or the slave, not both
                this.updatePendingDeviceMessage(offlineDeviceMessage);
            }
        }
        for (OfflineDeviceMessage deviceMessageShadow : this.device.getAllSentDeviceMessages()) {
            if (deviceMessageShadow.getDeviceId() == comTaskExecution.getDevice().getId()) {    //Only add the messages of the master or the slave, not both
                this.updateSentDeviceMessage(deviceMessageShadow);
            }
        }
    }

    private void updatePendingDeviceMessage(OfflineDeviceMessage offlineDeviceMessage) {
        if (this.allowedCategories.contains(offlineDeviceMessage.getSpecification().getCategory().getId())) {
            this.pendingMessages.add(offlineDeviceMessage);
        }
    }

    private void updateSentDeviceMessage(OfflineDeviceMessage offlineDeviceMessage) {
        if (this.allowedCategories.contains(offlineDeviceMessage.getSpecification().getCategory().getId())) {
            this.sentMessages.add(offlineDeviceMessage);
        }
    }

    private void createAllowedCategoryPrimaryKeyList() {
        this.allowedCategories = new ArrayList<>();
        this.allowedCategories.addAll(this.messagesTask.getDeviceMessageCategories().stream().map(DeviceMessageCategory::getId).collect(Collectors.toList()));
    }

    @Override
    public MessagesTask getMessagesTask() {
        return this.messagesTask;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        messagesCollectedData.add(deviceProtocol.updateSentMessages(this.sentMessages));
        messagesCollectedData.add(deviceProtocol.executePendingMessages(this.pendingMessages));
        messagesCollectedData.stream().forEach(collectedMessageList -> collectedMessageList.getCollectedMessages().forEach(collectedMessage -> collectedMessage.setDataCollectionConfiguration(comTaskExecution)));
        addListOfCollectedDataItems(messagesCollectedData);
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.MESSAGES_COMMAND;
    }
}
