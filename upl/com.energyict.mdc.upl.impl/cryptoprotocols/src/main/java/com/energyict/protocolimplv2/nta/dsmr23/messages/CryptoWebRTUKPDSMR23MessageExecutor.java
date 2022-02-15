package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Note that the breaker control messages are not supported in this class.
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 9:47
 * Author: khe
 */
public class CryptoWebRTUKPDSMR23MessageExecutor extends WebRTUKPDsmr23MessageExecutor {

    private final CommonCryptoMessageExecutor commonCryptoMessageExecutor;

    public CryptoWebRTUKPDSMR23MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
        this.commonCryptoMessageExecutor = new CommonCryptoMessageExecutor(protocol, collectedDataFactory, issueFactory);
    }


    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        // Separate master and slave messages
        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);

        // Filter out Mbus crypto messages
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices, handle crypto messages, otherwise send to normal executor
            result.addCollectedMessages(getMbusMessageExecutor().executePendingMessages(mbusMessages));
        }

        // *** Service Key Injection ***
        // Messages to change the AK, EK and HLS secret need to be combined, and executed separately
        ArrayList<OfflineDeviceMessage> globalKeyMessages = new ArrayList<>();
        Iterator<OfflineDeviceMessage> iterator = masterMessages.iterator();
        boolean needHLS = true; // Always needed for DSMR 4.x and 2.x

        while (iterator.hasNext()) {
            OfflineDeviceMessage pendingMessage = iterator.next();
            if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_PROCESS) ||
                    pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_PROCESS) ||
                    pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY_PROCESS)) {
                globalKeyMessages.add(pendingMessage);
                iterator.remove();
            }
        }

        // Execute Master messages before changing keys
        result.addCollectedMessages(super.executePendingMessages(masterMessages));

        // Execute the messages to change global keys combined!
        List<CollectedMessage> globalKeyMessageResults = new ArrayList<>();
        if (!globalKeyMessages.isEmpty()) {
            globalKeyMessageResults = commonCryptoMessageExecutor.changeGlobalKeysUsingServiceKeys(globalKeyMessages, needHLS);
        }

        for (CollectedMessage globalKeyMessageResult : globalKeyMessageResults) {
            result.addCollectedMessage(globalKeyMessageResult);
        }
        return result;
    }

    @Override
    protected AbstractMessageExecutor getMbusMessageExecutor() {
        return new CryptoDSMR23MbusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
    }

    @Override
    protected void renewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        commonCryptoMessageExecutor.renewKey(pendingMessage, keyAccessorTypeExtractor);
    }

    @Override
    protected void changeEncryptionKeyUsingServiceKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        commonCryptoMessageExecutor.changeEncryptionKeyUsingServiceKey(pendingMessage);
    }

    @Override
    protected void changeAuthenticationKeyUsingServiceKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        commonCryptoMessageExecutor.changeAuthenticationKeyUsingServiceKey(pendingMessage);
    }

    @Override
    protected void changeHLSSecretUsingServiceKey(OfflineDeviceMessage pendingMessage) throws IOException {
        commonCryptoMessageExecutor.changeHLSSecretUsingServiceKey(pendingMessage);
    }
}