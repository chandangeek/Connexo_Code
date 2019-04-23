package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.CryptoDSMR40MbusMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MessageExecutor;

import java.io.IOException;
import java.util.List;

/**
 * Note that the breaker control messages are not supported in this class.
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 9:47
 * Author: khe
 */
public class CryptoDSMR23MessageExecutor extends Dsmr23MessageExecutor {

    private final CommonCryptoMessageExecutor commonCryptoMessageExecutor;

    public CryptoDSMR23MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
        this.commonCryptoMessageExecutor = new CommonCryptoMessageExecutor(protocol, collectedDataFactory, issueFactory);
    }


    @Override
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
        result.addCollectedMessages(super.executePendingMessages(masterMessages));
        return result;
    }

    @Override
    protected AbstractMessageExecutor getMbusMessageExecutor() {
        return new CryptoDSMR23MbusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
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