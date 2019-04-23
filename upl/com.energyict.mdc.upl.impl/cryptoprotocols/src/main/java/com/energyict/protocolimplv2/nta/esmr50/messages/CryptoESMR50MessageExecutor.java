package com.energyict.protocolimplv2.nta.esmr50.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CryptoESMR50MessageExecutor extends ESMR50MessageExecutor {
    private final CommonCryptoMessageExecutor commonCryptoMessageExecutor;

    public CryptoESMR50MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
        this.commonCryptoMessageExecutor = new CommonCryptoMessageExecutor(protocol, collectedDataFactory, issueFactory);
    }

    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        //separate master and slave messages
        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);

        //filter out mbus crypto messages
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices, handle crypto messages, otherwise send to normal executor
            result.addCollectedMessages(getMbusMessageExecutor().executePendingMessages(mbusMessages));
        }
        //master messages
        List<OfflineDeviceMessage> notExecutedDeviceMessages = new ArrayList<>();
        for (OfflineDeviceMessage pendingMessage : masterMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {

                //TODO: ServiceKey not supported atm in Connexo
                if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY)) {
                    commonCryptoMessageExecutor.changeHLSSecretUsingServiceKey(pendingMessage);
                    collectedMessage = null;//
                    notExecutedDeviceMessages.add(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY)) {
                    commonCryptoMessageExecutor.changeAuthenticationKeyUsingServiceKey(pendingMessage);
                    collectedMessage = null;
                    notExecutedDeviceMessages.add(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY)) {
                    commonCryptoMessageExecutor.changeEncryptionKeyUsingServiceKey(pendingMessage);
                    collectedMessage = null;
                    notExecutedDeviceMessages.add(pendingMessage);
                } else
                {
                    collectedMessage = null;
                    notExecutedDeviceMessages.add(pendingMessage);
                }
            } catch (Exception e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse((IOException) e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            if (collectedMessage != null) {
                result.addCollectedMessage(collectedMessage);
            }
        }
        result.addCollectedMessages(super.executePendingMessages(notExecutedDeviceMessages));
        return result;
    }

    @Override
    protected CryptoESMR50MbusMessageExecutor getMbusMessageExecutor() {
        return new CryptoESMR50MbusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
    }
}
