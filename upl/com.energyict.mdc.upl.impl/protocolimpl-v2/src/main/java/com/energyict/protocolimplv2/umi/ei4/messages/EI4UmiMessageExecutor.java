package com.energyict.protocolimplv2.umi.ei4.messages;

import com.energyict.mdc.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.protocolimplv2.messages.UmiwanDeviceMessage;
import com.energyict.protocolimplv2.umi.ei4.EI4Umi;
import com.energyict.protocolimplv2.umi.ei4.events.EI4UmiLogBookFactory;
import com.energyict.protocolimplv2.umi.ei4.events.EI4UmiwanEventControl;
import com.energyict.protocolimplv2.umi.ei4.profile.EI4UmiProfileDataReader;
import com.energyict.protocolimplv2.umi.ei4.profile.EI4UmiwanProfileControl;
import com.energyict.protocolimplv2.umi.ei4.registers.EI4UmiRegisterFactory;
import com.energyict.protocolimplv2.umi.ei4.requests.EI4UmiCodesReader;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiGsmStdStatus;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiwanStdStatus;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class EI4UmiMessageExecutor {
    private static final String UMIWAN_STD_STATUS_CAS = "com.energyict.protocolimplv2.umi.UmiwanStdStatusCustomPropertySet";
    private static final String GSM_STD_STATUS_CAS = "com.energyict.protocolimplv2.umi.GsmStdStatusCustomPropertySet";

    private EI4Umi ei4Umi;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public EI4UmiMessageExecutor(EI4Umi ei4Umi, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.ei4Umi = ei4Umi;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(UmiwanDeviceMessage.SET_UMIWAN_CONFIGURATION)) {
                    collectedMessage = writeUmiwanConfiguration(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(UmiwanDeviceMessage.SET_UMIWAN_PROFILE_CONTROL)) {
                    collectedMessage = writeUmiwanProfileControl(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(UmiwanDeviceMessage.SET_UMIWAN_EVENT_CONTROL)) {
                    collectedMessage = writeUmiwanEventControl(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(UmiwanDeviceMessage.READ_UMIWAN_STD_STATUS)) {
                    collectedMessage = readUmiwanStdStatus(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(UmiwanDeviceMessage.READ_GSM_STD_STATUS)) {
                    collectedMessage = readGsmStdStatus(pendingMessage);
                } else {
                    String msg = "This message is not supported in the current protocol implementation";
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, msg));
                    collectedMessage.setDeviceProtocolInformation(msg);
                }
            } catch (IllegalArgumentException | IOException | IllegalStateException | IndexOutOfBoundsException e) {
                getProtocol().journal(Level.INFO, "Message execution has failed.", e);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e.getMessage()));
                collectedMessage.setDeviceProtocolInformation(e.toString());
            }
            result.addCollectedMessage(collectedMessage);
        }

        return result;
    }

    private CollectedMessage writeUmiwanEventControl(OfflineDeviceMessage pendingMessage) {
        EI4UmiLogBookFactory logBookFactory = new EI4UmiLogBookFactory(ei4Umi, issueFactory, collectedDataFactory);
        return createCollectedMessageWithUmiwanEventControl(pendingMessage, logBookFactory.setUmiwanEventControl(pendingMessage));
    }

    private CollectedMessage writeUmiwanProfileControl(OfflineDeviceMessage pendingMessage) {
        EI4UmiProfileDataReader profileDataReader = new EI4UmiProfileDataReader(ei4Umi, collectedDataFactory, issueFactory, ei4Umi.getOfflineDevice());
        return createCollectedMessageWithUmiwanProfileControl(pendingMessage, profileDataReader.setUmiwanProfileControl(pendingMessage));
    }

    private CollectedMessage writeUmiwanConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        EI4UmiRegisterFactory registerFactory = new EI4UmiRegisterFactory(ei4Umi, issueFactory, collectedDataFactory);
        registerFactory.setUmiwanConfiguration(pendingMessage);
        return createCollectedMessageWithUmiwanConfiguration(pendingMessage);
    }

    private CollectedMessage readUmiwanStdStatus(OfflineDeviceMessage pendingMessage) throws IOException {
        EI4UmiCodesReader configurationReader = new EI4UmiCodesReader(ei4Umi, issueFactory, ei4Umi.getOfflineDevice());
        return createCollectedMessageWithUmiwanStdStatus(pendingMessage, configurationReader.getUmiwanStdStatus());
    }

    private CollectedMessage readGsmStdStatus(OfflineDeviceMessage pendingMessage) throws IOException {
        EI4UmiCodesReader configurationReader = new EI4UmiCodesReader(ei4Umi, issueFactory, ei4Umi.getOfflineDevice());
        return createCollectedMessageWithGsmStdStatus(pendingMessage, configurationReader.getGsmStdStatus());
    }

    public CollectedMessage createCollectedMessage(OfflineDeviceMessage pendingMessage) {
        CollectedMessage collectedMessage = this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(pendingMessage.getDeviceMessageId(), pendingMessage.getDeviceIdentifier()));
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessage;
    }

    public Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueFactory
                .createWarning(
                        pendingMessage,
                        "DeviceMessage.failed",
                        pendingMessage.getDeviceMessageId(),
                        pendingMessage.getSpecification().getCategory().getName(),
                        pendingMessage.getSpecification().getName(),
                        message);
    }

    private CollectedMessage createCollectedMessageWithUmiwanConfiguration(OfflineDeviceMessage message) {
        CollectedMessage collectedMessageWithUmiwanConfiguration = this.collectedDataFactory.createCollectedMessage(message.getMessageIdentifier());
        collectedMessageWithUmiwanConfiguration.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessageWithUmiwanConfiguration;
    }

    private CollectedMessage createCollectedMessageWithUmiwanProfileControl(OfflineDeviceMessage message, EI4UmiwanProfileControl profileControl) {
        CollectedMessage collectedMessageWithUmiwanProfileControl = this.collectedDataFactory.createCollectedMessageWithUmiwanProfileControl(new DeviceMessageIdentifierById(message.getDeviceMessageId(),
                message.getDeviceIdentifier()), profileControl.getStartTime());
        collectedMessageWithUmiwanProfileControl.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessageWithUmiwanProfileControl;
    }

    private CollectedMessage createCollectedMessageWithUmiwanEventControl(OfflineDeviceMessage pendingMessage, EI4UmiwanEventControl umiwanEventControl) {
        CollectedMessage collectedMessageWithUmiwanEventControl = this.collectedDataFactory.createCollectedMessageWithUmiwanEventControl(new DeviceMessageIdentifierById(pendingMessage.getDeviceMessageId(),
                pendingMessage.getDeviceIdentifier()), umiwanEventControl.getStartTime(), umiwanEventControl.getControlFlags(), umiwanEventControl.getAcknowledgeFlags());
        collectedMessageWithUmiwanEventControl.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessageWithUmiwanEventControl;
    }

    private CollectedMessage createCollectedMessageWithUmiwanStdStatus(OfflineDeviceMessage message, UmiwanStdStatus umiwanStdStatus) {
        CollectedMessage collectedMessageWithUmiwanConfiguration = this.collectedDataFactory.createCollectedMessageWithUmiwanStructure(
                new DeviceMessageIdentifierById(message.getDeviceMessageId(), message.getDeviceIdentifier()),
                umiwanStdStatus.toMap(),
                UMIWAN_STD_STATUS_CAS);
        collectedMessageWithUmiwanConfiguration.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessageWithUmiwanConfiguration;
    }

    private CollectedMessage createCollectedMessageWithGsmStdStatus(OfflineDeviceMessage message, UmiGsmStdStatus gsmStdStatus) {
        CollectedMessage collectedMessageWithUmiwanConfiguration = this.collectedDataFactory.createCollectedMessageWithUmiwanStructure(
                new DeviceMessageIdentifierById(message.getDeviceMessageId(), message.getDeviceIdentifier()),
                gsmStdStatus.toMap(),
                GSM_STD_STATUS_CAS);
        collectedMessageWithUmiwanConfiguration.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessageWithUmiwanConfiguration;
    }

    public EI4Umi getProtocol() {
        return this.ei4Umi;
    }
}
