package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.MessagesCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.tasks.MessagesTask;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a {@link MessagesCommand}
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/06/12
 * Time: 14:16
 */
public class MessagesCommandImpl extends SimpleComCommand implements MessagesCommand {

    /**
     * The task used for modeling this command
     */
    private final ServerMessagesTask messagesTask;
    private final OfflineDevice device;
    private DeviceMessageSpecFactory deviceMessageSpecFactory;
    private List<OfflineDeviceMessage> pendingMessages;
    private List<OfflineDeviceMessage> sentMessages;
    private List<String> allowedCategories;
    private List<String> allowedSpecs;

    public MessagesCommandImpl(final ServerMessagesTask messagesTask, final OfflineDevice device, final CommandRoot commandRoot) {
        super(commandRoot);
        if (messagesTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "messagesTask");
        }
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device");
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot");
        }
        this.messagesTask = messagesTask;
        this.device = device;
        createAllowedCategoryPrimaryKeyList();
        createAllowedSpecPrimaryKeyList();
        updateMessageLists();
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        this.appendPendingMessages(builder);
        this.appendSentMessages(builder);
    }

    private void appendPendingMessages (DescriptionBuilder sb) {
        this.appendMessages(sb, this.pendingMessages, "There are no pending messages", "pendingMessages");
    }

    private void appendSentMessages (DescriptionBuilder sb) {
        this.appendMessages(sb, this.sentMessages, "The are no messages to update from previous sessions", "messagesFromPreviousSession");
    }

    private void appendMessages (DescriptionBuilder builder, List<OfflineDeviceMessage> messages, String noMessagesDescription, String description) {
        if (messages.isEmpty()) {
            builder.addLabel(noMessagesDescription);
        }
        else {
            PropertyDescriptionBuilder descriptionBuilder = builder.addListProperty(description);
            for (OfflineDeviceMessage message : messages) {
                descriptionBuilder.append(message.getDeviceMessageId());
                descriptionBuilder.append(" (");
                descriptionBuilder.append(message.getSpecification().getName());
                descriptionBuilder.append(", ");
                descriptionBuilder.append(message.getSpecification().getCategory().getName());
                descriptionBuilder.append(")");
                descriptionBuilder = descriptionBuilder.next();
            }
        }
    }

    private DeviceMessageSpecFactory getDeviceMessageSpecFactory() {
        if (this.deviceMessageSpecFactory == null) {
            this.deviceMessageSpecFactory = ManagerFactory.getCurrent().getDeviceMessageSpecFactory();
        }
        return this.deviceMessageSpecFactory;
    }

    private void updateMessageLists() {
        this.pendingMessages = new ArrayList<>();
        this.sentMessages = new ArrayList<>();
        for (OfflineDeviceMessage offlineDeviceMessage : this.device.getAllPendingDeviceMessages()) {
            this.updatePendingDeviceMessage(offlineDeviceMessage);
        }
        for (OfflineDeviceMessage deviceMessageShadow : this.device.getAllSentDeviceMessages()) {
            this.updateSentDeviceMessage(deviceMessageShadow);
        }
    }

    private void updatePendingDeviceMessage(OfflineDeviceMessage offlineDeviceMessage) {
        if (this.allowedCategories.contains(getDeviceMessageSpecFactory().fromPrimaryKey(offlineDeviceMessage.getDeviceMessageSpecPrimaryKey().getValue()).getCategory().getPrimaryKey().getValue())
                || this.allowedSpecs.contains(offlineDeviceMessage.getDeviceMessageSpecPrimaryKey().getValue())
                || this.messagesTask.isAllCategories()) {
            this.pendingMessages.add(offlineDeviceMessage);
        }
    }

    private void updateSentDeviceMessage(OfflineDeviceMessage deviceMessageShadow) {
        if (this.allowedCategories.contains(getDeviceMessageSpecFactory().fromPrimaryKey(deviceMessageShadow.getDeviceMessageSpecPrimaryKey().getValue()).getCategory().getPrimaryKey().getValue())
                || this.allowedSpecs.contains(deviceMessageShadow.getDeviceMessageSpecPrimaryKey().getValue())
                || this.messagesTask.isAllCategories()) {
            this.sentMessages.add(deviceMessageShadow);
        }
    }

    private void createAllowedCategoryPrimaryKeyList() {
        this.allowedCategories = new ArrayList<>();
        for (DeviceMessageCategory deviceMessageCategory : this.messagesTask.getDeviceMessageCategories()) {
            this.allowedCategories.add(deviceMessageCategory.getPrimaryKey().getValue());
        }
    }

    private void createAllowedSpecPrimaryKeyList() {
        this.allowedSpecs = new ArrayList<>();
        for (DeviceMessageSpec deviceMessageSpec : this.messagesTask.getDeviceMessageSpecs()) {
            this.allowedSpecs.add(deviceMessageSpec.getPrimaryKey().getValue());
        }
    }

    @Override
    public MessagesTask getMessagesTask() {
        return this.messagesTask;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        addCollectedDataItem(deviceProtocol.updateSentMessages(this.sentMessages));
        addCollectedDataItem(deviceProtocol.executePendingMessages(this.pendingMessages));
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.MESSAGES_COMMAND;
    }

    protected List<OfflineDeviceMessage> getPendingMessages() {
        return pendingMessages;
    }

    protected List<OfflineDeviceMessage> getSentMessages() {
        return sentMessages;
    }

}