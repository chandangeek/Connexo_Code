package com.energyict.protocolimplv2.dlms.idis.am540.messaging;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540MessageExecutor;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/10/2016 - 10:27
 */
public class CryptoAM540MessageExecutor extends AM540MessageExecutor {

    private final CommonCryptoMessageExecutor executor;

    public CryptoAM540MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        this.executor = new CommonCryptoMessageExecutor(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected CollectedMessage changeAuthenticationKeyAndUseNewKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode clientSecuritySetupObis = getClientSecuritySetupObis(offlineDeviceMessage);
        int clientId = getClientId(offlineDeviceMessage);
        executor.changeAuthKey(offlineDeviceMessage, clientSecuritySetupObis, clientId);
        return collectedMessage;
    }

    @Override
    protected CollectedMessage changeEncryptionKeyAndUseNewKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode clientSecuritySetupObis = getClientSecuritySetupObis(offlineDeviceMessage);
        int clientId = getClientId(offlineDeviceMessage);
        executor.changeEncryptionKey(offlineDeviceMessage, clientSecuritySetupObis, clientId);
        return collectedMessage;
    }

    @Override
    protected CollectedMessage changeMasterKeyAndUseNewKey(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        ObisCode clientSecuritySetupObis = getClientSecuritySetupObis(offlineDeviceMessage);
        executor.changeMasterKey(offlineDeviceMessage, clientSecuritySetupObis);
        return collectedMessage;
    }

}