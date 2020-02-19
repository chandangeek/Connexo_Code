package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.common.CommonCryptoMbusMessageExecutor;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.common.CryptoDSMR40Properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CryptoDSMR40MbusMessageExecutor extends Dsmr40MbusMessageExecutor {

    private CommonCryptoMbusMessageExecutor mbusCryptoMessageExecutor;

    public CryptoDSMR40MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        this.mbusCryptoMessageExecutor = new CommonCryptoMbusMessageExecutor(isUsingCryptoServer(), getProtocol(), collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        List<OfflineDeviceMessage> messagesForSuper = new ArrayList<>();
        CollectedMessageList collectedMessages = getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.SetEncryptionKeysUsingCryptoserver)) {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
                try {
                    mbusCryptoMessageExecutor.setCryptoserverMbusEncryptionKeys(pendingMessage);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession().getProperties().getRetries() + 1)) {
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                        collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    }
                }
                collectedMessages.addCollectedMessage(collectedMessage);
            }
            else {
                messagesForSuper.add(pendingMessage);
            }
        }
        collectedMessages.addCollectedMessages(super.executePendingMessages(messagesForSuper));
        return collectedMessages;
    }

    private boolean isUsingCryptoServer() {
        AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol)getProtocol();
        return ((CryptoDSMR40Properties) protocol.getDlmsSessionProperties()).useCryptoServer();
    }
}
