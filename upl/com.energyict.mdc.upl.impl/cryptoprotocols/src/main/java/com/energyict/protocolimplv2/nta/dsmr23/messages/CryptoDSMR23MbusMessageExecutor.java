package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.common.CommonCryptoMbusMessageExecutor;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23Properties;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MbusMessageExecutor;

import java.io.IOException;

public class CryptoDSMR23MbusMessageExecutor extends Dsmr23MbusMessageExecutor {
    private CommonCryptoMbusMessageExecutor mbusCryptoMessageExecutor = null;
    public CryptoDSMR23MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        this.mbusCryptoMessageExecutor = new CommonCryptoMbusMessageExecutor(isUsingCryptoServer(), getProtocol(), collectedDataFactory, issueFactory);
    }

    @Override
    protected void setCryptoserverMbusEncryptionKeys(OfflineDeviceMessage pendingMessage) throws IOException {
        mbusCryptoMessageExecutor.setCryptoserverMbusEncryptionKeys(pendingMessage);
    }

    private boolean isUsingCryptoServer() {
        AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol)getProtocol();
        return ((CryptoDSMR23Properties) protocol.getDlmsSessionProperties()).useCryptoServer();
    }
}
