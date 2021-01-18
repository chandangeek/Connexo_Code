package com.energyict.protocolimplv2.dlms.common.writers.impl;

import com.energyict.mdc.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.protocolimplv2.dlms.common.writers.Message;

public abstract class AbstractMessage implements Message {

    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public AbstractMessage(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    protected CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId(), message.getDeviceIdentifier()));
    }

    public CollectedMessage createNotSupportedMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId(), message.getDeviceIdentifier()));
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(message));
        collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        return collectedMessage;
    }

    protected CollectedMessage createErrorCollectedMessage(OfflineDeviceMessage message, Exception e) {
        CollectedMessage collectedMessage = collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId(), message.getDeviceIdentifier()));
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        collectedMessage.setFailureInformation(ResultType.Other, createError(message, e));
        collectedMessage.setDeviceProtocolInformation("Error encountered during execution:" + e.getMessage());
        return collectedMessage;
    }

    protected OfflineDeviceMessageAttribute getMessageAttribute(OfflineDeviceMessage message, String attName) throws ProtocolException {
        return message.getDeviceMessageAttributes().stream().filter(f -> f.getName().equals(attName)).findFirst().orElseThrow(() -> new ProtocolException("DeviceMessage didn't contain a value found for MessageAttribute " + attName));
    }

    private Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    private Issue createError(OfflineDeviceMessage pendingMessage, Exception e) {
        return this.issueFactory.createProblem(pendingMessage, e.getMessage(),
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

}
