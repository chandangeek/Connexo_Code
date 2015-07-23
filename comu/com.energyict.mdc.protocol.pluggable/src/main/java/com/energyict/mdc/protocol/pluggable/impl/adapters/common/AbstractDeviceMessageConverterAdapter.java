package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.exception.MessageSeed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for implementing the {@link DeviceMessageSupport}
 * for legacy protocols.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:27
 */
public abstract class AbstractDeviceMessageConverterAdapter implements DeviceMessageSupport {

    private static final int UPDATE_SENT_MASK = 0b0001;
    private static final int EXECUTE_PENDING_MASK = 0b0010;

    private final DataModel dataModel;
    private final MessageAdapterMappingFactory messageAdapterMappingFactory;
    private final ProtocolPluggableService protocolPluggableService;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private MessageProtocol messageProtocol;
    private String serialNumber = "";

    /**
     * Mask indicating that both method calls are made.
     */
    private static final int FIRE_MESSAGES_MASK = UPDATE_SENT_MASK | EXECUTE_PENDING_MASK;

    /**
     * The messageConverter which will be used.
     */
    private LegacyMessageConverter legacyMessageConverter;

    /**
     * Serves as a tracker whether or not both method calls ({@link DeviceMessageSupport#updateSentMessages(java.util.List)}
     * and {@link DeviceMessageSupport#executePendingMessages(java.util.List)}) are called.
     * Only if both are called we delegate to the protocol
     */
    private int fireMessagesTracker = 0b0000;

    /**
     * Indication whether the legacy protocol supports messaging.
     */
    private boolean messagesAreSupported = true;

    private Map<MessageEntry, OfflineDeviceMessage> messageEntries = new HashMap<>();

    protected AbstractDeviceMessageConverterAdapter(DataModel dataModel, MessageAdapterMappingFactory messageAdapterMappingFactory, ProtocolPluggableService protocolPluggableService, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super();
        this.dataModel = dataModel;
        this.messageAdapterMappingFactory = messageAdapterMappingFactory;
        this.protocolPluggableService = protocolPluggableService;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
    }

    /**
     * Creates a new instance of a {@link LegacyMessageConverter} component based on the given className.
     *
     * @param className the className of the LegacyMessageConverter
     * @return the newly created instance
     */
    protected Object createNewMessageConverterInstance(String className) {
        return this.getProtocolPluggableService().createDeviceProtocolMessagesFor(className);
    }

    public LegacyMessageConverter getLegacyMessageConverter() {
        return legacyMessageConverter;
    }

    public void setLegacyMessageConverter(LegacyMessageConverter legacyMessageConverter) {
        this.legacyMessageConverter = legacyMessageConverter;
    }

    protected ProtocolPluggableService getProtocolPluggableService() {
        return protocolPluggableService;
    }

    private void fireUpdateSentMessages() {
        this.fireMessagesTracker |= UPDATE_SENT_MASK;
    }

    private void fireExecutePendingMessages() {
        this.fireMessagesTracker |= EXECUTE_PENDING_MASK;
    }

    private boolean fireMessagesToProtocol() {
        return this.fireMessagesTracker == FIRE_MESSAGES_MASK;
    }

    protected void noSupportForMessagingRequired() {
        this.messagesAreSupported = false;
    }

    protected boolean messagesAreSupported() {
        return this.messagesAreSupported;
    }

    private void addMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        for (OfflineDeviceMessage offlineDeviceMessage : offlineDeviceMessages) {
            final MessageEntry messageEntry = getLegacyMessageConverter().toMessageEntry(offlineDeviceMessage);
            messageEntry.setSerialNumber(this.serialNumber);
            this.messageEntries.put(messageEntry, offlineDeviceMessage);
        }
    }

    private CollectedMessageList delegateMessageEntriesToLegacyProtocol() {
        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        CollectedMessageList collectedMessageList = collectedDataFactory.createCollectedMessageList(new ArrayList<>(messageEntries.values()));
        boolean continueOnSendingPendingMessages = true;
        try {
            this.messageProtocol.applyMessages(new ArrayList<>(messageEntries.keySet()));
        } catch (IOException e) {
            continueOnSendingPendingMessages = false;
            collectedMessageList.setFailureInformation(ResultType.Other, getIssue(this.messageProtocol, MessageSeeds.MESSAGEADAPTER_APPLYMESSAGES_ISSUE, e.getMessage()));
        }
        if (continueOnSendingPendingMessages) {
            for (Map.Entry<MessageEntry, OfflineDeviceMessage> messageEntryMapElement : messageEntries.entrySet()) {
                collectedMessageList.addCollectedMessages(
                        delegatePendingMessageToProtocol(messageEntryMapElement.getKey(), messageEntryMapElement.getValue()));
            }
        }

        return collectedMessageList;
    }

    private Issue getIssue(Object source, MessageSeed description, Object... arguments){
        return this.issueService.newProblem(source, description.getKey(), arguments);
    }

    private CollectedMessage delegatePendingMessageToProtocol(MessageEntry messageEntry, OfflineDeviceMessage offlineDeviceMessage) {
        MessageResult messageResult;
        CollectedMessage collectedMessage;
        collectedMessage = this.collectedDataFactory.createCollectedMessage(offlineDeviceMessage.getIdentifier());
        try {
            messageResult = this.messageProtocol.queryMessage(messageEntry);
            collectedMessage.setNewDeviceMessageStatus(getNewDeviceMessageStatus(messageResult));
            if (!messageResult.getInfo().isEmpty()) {
                collectedMessage.setDeviceProtocolInformation(messageResult.getInfo());
            }
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
        }
        return collectedMessage;
    }

    private DeviceMessageStatus getNewDeviceMessageStatus(MessageResult messageResult) {
        if (messageResult.isSuccess()) {
            return DeviceMessageStatus.CONFIRMED;
        } else if (messageResult.isFailed()) {
            return DeviceMessageStatus.FAILED;
        } else if (messageResult.isQueued()) {
            return DeviceMessageStatus.SENT;
        } else {
            return DeviceMessageStatus.INDOUBT;
        }
    }

    protected void setMessageProtocol(MessageProtocol meterProtocol) {
        this.messageProtocol = meterProtocol;
    }

    protected CollectedMessageList getNoopCollectedMessageList() {
        return this.collectedDataFactory.createEmptyCollectedMessageList();
    }

    /**
     * @return a <code>List</code> of Standard supported messages
     */
    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getLegacyMessageConverter().getSupportedMessages();
    }

    /**
     * Handle all given <code>Messages</code>. Each message should return a result so proper handling of the message can be done.
     *
     * @param pendingMessages the pending messages on a Device
     * @return Message results for the provided pending messages.
     */
    @Override
    public CollectedMessageList executePendingMessages(final List<OfflineDeviceMessage> pendingMessages) {
        if (messagesAreSupported()) {
            fireExecutePendingMessages();
            addMessages(pendingMessages);
            if (fireMessagesToProtocol()) {
                return delegateMessageEntriesToLegacyProtocol();
            }
            return getNoopCollectedMessageList();
        } else {
            return getNoopCollectedMessageList();
        }
    }

    /**
     * Handle all given {@link OfflineDeviceMessage}s which have previously been sent to the device.
     *
     * @param sentMessages the sent messages
     * @return CollectedData containing update information about the sent messages
     */
    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        if (messagesAreSupported()) {
            fireUpdateSentMessages();
            addMessages(sentMessages);
            if (fireMessagesToProtocol()) {
                return delegateMessageEntriesToLegacyProtocol();
            }
            return getNoopCollectedMessageList();
        } else {
            return getNoopCollectedMessageList();
        }
    }

    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (messagesAreSupported()) {
            return getLegacyMessageConverter().format(propertySpec, messageAttribute);
        } else {
            return "";
        }
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Get the className of the LegacyMessageConverter based on the given MeterProtocol.
     *
     * @param deviceProtocolJavaClassname the javaClassName of the protocol to match
     * @return the className of the LegacyMessageConverter to use
     */
    protected String getDeviceMessageConverterMappingFor(String deviceProtocolJavaClassname) {
        String meterProtocolClassName = getMessageAdapterMappingFactory().getMessageMappingJavaClassNameForDeviceProtocol(deviceProtocolJavaClassname);
        if (meterProtocolClassName == null) {
            throw DeviceProtocolAdapterCodingExceptions.mappingElementDoesNotExist(MessageSeeds.NON_EXISTING_MAP_ELEMENT, this.getClass(), "legacyMessageMapper", deviceProtocolJavaClassname);
        }
        return meterProtocolClassName;
    }

    private MessageAdapterMappingFactory getMessageAdapterMappingFactory() {
        return this.messageAdapterMappingFactory;
    }

}
