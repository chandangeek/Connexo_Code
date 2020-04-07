package com.energyict.protocolimplv2.nta.esmr50.messages;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CryptoESMR50MessageExecutor extends ESMR50MessageExecutor {

    private final CommonCryptoMessageExecutor commonCryptoMessageExecutor;
    private final DeviceMasterDataExtractor deviceMasterDataExtractor;

    public CryptoESMR50MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory,
                                       IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor,
                                       DeviceMasterDataExtractor deviceMasterDataExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor, deviceMasterDataExtractor);
        this.commonCryptoMessageExecutor = new CommonCryptoMessageExecutor(protocol, collectedDataFactory, issueFactory);
        this.deviceMasterDataExtractor = deviceMasterDataExtractor;
    }

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
        boolean needHLS = false; // Optional for ESMR 5

        while (iterator.hasNext()) {
            OfflineDeviceMessage pendingMessage = iterator.next();
            if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY_PROCESS)) {
                needHLS = true;
            }
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
    protected CryptoESMR50MbusMessageExecutor getMbusMessageExecutor() {
        return new CryptoESMR50MbusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory(),
                this.deviceMasterDataExtractor);
    }

    @Override
    protected void renewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        commonCryptoMessageExecutor.renewKey(pendingMessage, keyAccessorTypeExtractor);
    }

}
