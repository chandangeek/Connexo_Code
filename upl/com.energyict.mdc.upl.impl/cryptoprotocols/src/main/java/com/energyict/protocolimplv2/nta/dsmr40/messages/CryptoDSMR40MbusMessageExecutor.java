package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.common.CommonCryptoMbusMessageExecutor;
import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MbusMessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.MBusKeyID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CryptoDSMR40MbusMessageExecutor extends Dsmr40MbusMessageExecutor {
    private CommonCryptoMbusMessageExecutor mbusCryptoMessageExecutor = null;
    public CryptoDSMR40MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        this.mbusCryptoMessageExecutor = new CommonCryptoMbusMessageExecutor(isUsingCryptoServer(), getProtocol(), collectedDataFactory, issueFactory);
    }

    @Override
    protected void setCryptoserverMbusEncryptionKeys(OfflineDeviceMessage pendingMessage) throws IOException {
        mbusCryptoMessageExecutor.setCryptoserverMbusEncryptionKeys(pendingMessage);
    }

    private boolean isUsingCryptoServer() {
        AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol)getProtocol();
        return ((CryptoDSMR23Properties) protocol.getProperties()).useCryptoServer();
    }
}
